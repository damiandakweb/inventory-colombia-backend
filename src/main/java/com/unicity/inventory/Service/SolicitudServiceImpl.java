package com.unicity.inventory.Service;

import com.unicity.inventory.Mapping.*;
import com.unicity.inventory.Models.*;
import com.unicity.inventory.Repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolicitudServiceImpl  implements  SolicitudService {

    private final SolicitudRepository solicitudRepository;

    private final SolicitudMapping solicitudMapping;

    private final UsuarioRepository usuarioRepository;

    private final CategoriaRepository categoriaRepository;

    private final MovimientoServiceImpl movimientoService;

    private final MovimientoRepository movimientoRepository;

    private final UbicacionRepository ubicacionRepository;

    private final ActivoRepository activoRepository;

    private final EstadoRepository estadoRepository;

    private final EmailService emailService;

    @Override
    public boolean existByIdSolicitud(Long id) {
        return solicitudRepository.existsById(id);
    }

    @Override
    @Transactional
    public void crearNuevaSolicitud(SolicitudDto dto) {
        // Validación 1: Asegurarse de que la marca de tiempo exista
        if (dto.getMarcaTemporalFuente() == null || dto.getMarcaTemporalFuente().isBlank()) {
            System.out.println("Solicitud ignorada: no contiene marca temporal.");
            return;
        }

        // Validación 2: Usar el método del repositorio para verificar duplicados
        if (solicitudRepository.existsByMarcaTemporalFuente(dto.getMarcaTemporalFuente())) {
            System.out.println("Solicitud duplicada ignorada (marca temporal ya existe): " + dto.getMarcaTemporalFuente());
            return;
        }

        // --- LÓGICA CENTRALIZADA Y SIN CONDICIONES ---
        // Si la solicitud es nueva y no es un duplicado, SIEMPRE generamos un Ticket ID
        String nuevoTicketId = generarSiguienteTicketId();

        // Buscamos las entidades relacionadas
        Usuario usuario = usuarioRepository.findByNombre(dto.getNombreUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + dto.getNombreUsuario()));
        Categoria categoria = categoriaRepository.findByNombreCategoria(dto.getNombreCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + dto.getNombreCategoria()));

        // Creamos la nueva entidad Solicitud
        Solicitud nuevaSolicitud = new Solicitud();
        nuevaSolicitud.setTicketId(nuevoTicketId); // Asignamos el nuevo Ticket ID
        nuevaSolicitud.setTipoSolicitud(dto.getTipoSolicitud());
        nuevaSolicitud.setEstadoSolicitud("Nuevo");
        nuevaSolicitud.setFechaSolicitud(LocalDate.now());
        nuevaSolicitud.setUsuario(usuario);
        nuevaSolicitud.setCategoria(categoria);
        nuevaSolicitud.setMarcaTemporalFuente(dto.getMarcaTemporalFuente());

        // Guardamos en la base de datos
        // El try-catch aquí es una segunda capa de seguridad en caso de condiciones de carrera
        try {
            solicitudRepository.save(nuevaSolicitud);
            System.out.println("Nueva solicitud creada con Ticket ID: " + nuevoTicketId);
        } catch (DataIntegrityViolationException e) {
            System.out.println("Solicitud duplicada detectada por la BD (condición de carrera): " + dto.getMarcaTemporalFuente());
        }
    }

    /**
     * Genera el siguiente ID de ticket secuencial para el año actual.
     * Es 'synchronized' para ser seguro en entornos con múltiples hilos.
     */
    private synchronized String generarSiguienteTicketId() {
        int anioActual = Year.now().getValue();
        String prefijo = "SOL-" + anioActual + "-";

        // Busca el último ticket de este año para determinar el siguiente número
        String ultimoTicket = solicitudRepository.findTopByTicketIdStartingWithOrderByTicketIdDesc(prefijo)
                .map(Solicitud::getTicketId)
                .orElse(prefijo + "00000"); // Si no hay ninguno este año, empezamos en 0

        // Extrae el número, lo incrementa y le da formato de 5 dígitos (ej: 00001)
        int ultimoNumero = Integer.parseInt(ultimoTicket.substring(prefijo.length()));
        int nuevoNumero = ultimoNumero + 1;

        return prefijo + String.format("%05d", nuevoNumero);
    }


    @Override
    public boolean deleteSolicitud(Long id) {
        if (solicitudRepository.existsById(id)) {
            solicitudRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<SolicitudDto> getAllSolicitudes() {
        return solicitudRepository.findAll().stream()
                .map(solicitudMapping::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void procesarSolicitud(Long solicitudId, ProcesarSolicitudRequest request) {
            // 1. Buscamos las entidades principales (sin cambios)
            Solicitud solicitud = solicitudRepository.findById(solicitudId)
                    .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
            Usuario usuario = solicitud.getUsuario();
            Ubicacion ubicacionDestino = ubicacionRepository.findById(request.getIdUbicacion())
                    .orElseThrow(() -> new RuntimeException("Ubicación de destino no encontrada"));
            Activo activoNuevo = activoRepository.findById(request.getIdActivoNuevo())
                    .orElseThrow(() -> new RuntimeException("Activo de reemplazo no encontrado"));

            Ubicacion ubicacionBodega = ubicacionRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("Ubicación 'Bodega' no encontrada"));

            Activo activoViejo = null;

            // 2. Si se especificó un ACTIVO VIEJO, lo procesamos
            if (request.getIdActivoViejo() != null) {
                 activoViejo = activoRepository.findById(request.getIdActivoViejo())
                        .orElseThrow(() -> new RuntimeException("Activo viejo no encontrado"));

                // --- 👇 INICIO DE LA LÓGICA MEJORADA ---
                Estado estadoParaActivoViejo;
                String tipoMovimientoDevolucion;
                String tipoSolicitud = solicitud.getTipoSolicitud().toUpperCase();

                // CASO 1: La solicitud es por un equipo dañado o con fallos.
                if (tipoSolicitud.contains("DAÑO") || tipoSolicitud.contains("FALLO")) {
                    // Asignamos el estado "Dañado" (asumiendo ID 3)
                    estadoParaActivoViejo = estadoRepository.findById(3L)
                            .orElseThrow(() -> new RuntimeException("Estado 'Dañado' (ID 3) no encontrado"));
                    tipoMovimientoDevolucion = "Devolución por Daño/Fallo";
                }
                // CASO 2: La solicitud es para enviar a mantenimiento.
                else if (tipoSolicitud.contains("MANTENIMIENTO")) {
                    // Asignamos el estado "En Mantenimiento" (asumiendo ID 4)
                    estadoParaActivoViejo = estadoRepository.findById(4L)
                            .orElseThrow(() -> new RuntimeException("Estado 'En mantenimiento' (ID 4) no encontrado"));
                    tipoMovimientoDevolucion = "Devolución para Mantenimiento";
                }
                // CASO 3 (Por defecto): Cualquier otra devolución (ej. fin de contrato, etc.)
                else {
                    // El activo vuelve a estar "Disponible" (asumiendo ID 1)
                    estadoParaActivoViejo = estadoRepository.findById(1L)
                            .orElseThrow(() -> new RuntimeException("Estado 'Disponible' (ID 1) no encontrado"));
                    tipoMovimientoDevolucion = "Devolución por Reemplazo";
                }
                // --- FIN DE LA LÓGICA MEJORADA ---

                activoViejo.setEstado(estadoParaActivoViejo);
                activoViejo.setUsuarioActual(null); // Lo desasignamos

                // Creamos su movimiento de devolución
                Movimiento movDevolucion = new Movimiento();
                movDevolucion.setTipoDeMovimiento(tipoMovimientoDevolucion);
                movDevolucion.setFechaMovimiento(LocalDate.now());
                movDevolucion.setActivo(activoViejo);
                movDevolucion.setUsuario(usuario);
                movDevolucion.setUbicacion(ubicacionBodega);
                movimientoRepository.save(movDevolucion);
            }

            // 3. Procesamos el ACTIVO NUEVO (sin cambios)
            Estado estadoEnUso = estadoRepository.findById(2L)
                    .orElseThrow(() -> new RuntimeException("Estado 'En uso' no encontrado"));
            activoNuevo.setEstado(estadoEnUso);
            activoNuevo.setUsuarioActual(usuario);

            // Creamos su movimiento de asignación
            Movimiento movAsignacion = new Movimiento();
            movAsignacion.setTipoDeMovimiento(solicitud.getTipoSolicitud());
            movAsignacion.setFechaMovimiento(LocalDate.now());
            movAsignacion.setObservacion(request.getObservacion());
            movAsignacion.setActivo(activoNuevo);
            movAsignacion.setUsuario(usuario);
            movAsignacion.setUbicacion(ubicacionDestino);
            movimientoRepository.save(movAsignacion);

            // 4. Actualizamos la solicitud a "Procesado" (sin cambios)
            solicitud.setEstadoSolicitud("Procesado");
            solicitud.setMovimiento(movAsignacion);
            solicitudRepository.save(solicitud);

            enviarCorreoDeEntregaSiAplica(solicitud, activoNuevo, activoViejo);
    }


    @Override
    @Transactional
    public void procesarSolicitudDeCambio(Long solicitudId, ProcesarSolicitudRequest request) {
        // 1. Buscamos todas las entidades principales
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        Usuario usuario = solicitud.getUsuario();
        Ubicacion ubicacionDestino = ubicacionRepository.findById(request.getIdUbicacion())
                .orElseThrow(() -> new RuntimeException("Ubicación de destino no encontrada"));
        Activo activoNuevo = activoRepository.findById(request.getIdActivoNuevo())
                .orElseThrow(() -> new RuntimeException("Activo de reemplazo no encontrado"));

        // Buscamos los estados que vamos a necesitar
        Estado estadoEnUso = estadoRepository.findById(2L).orElseThrow(() -> new RuntimeException("Estado 'En uso' no encontrado"));
        Estado estadoEnBodega = estadoRepository.findById(1L).orElseThrow(() -> new RuntimeException("Estado 'Disponible/Nuevo' no encontrado"));

        // Asumimos que la ubicación de Bodega/Taller tiene el ID 1
        Ubicacion ubicacionBodega = ubicacionRepository.findById(1L).orElseThrow(() -> new RuntimeException("Ubicación 'Bodega' no encontrada"));

        Activo activoViejo = null;


        // 2. Si hay un ACTIVO VIEJO, lo procesamos (lo devolvemos a bodega)
        if (request.getIdActivoViejo() != null) {
             activoViejo = activoRepository.findById(request.getIdActivoViejo())
                    .orElseThrow(() -> new RuntimeException("Activo viejo no encontrado"));

            activoViejo.setEstado(estadoEnBodega);
            activoViejo.setUsuarioActual(null); // Lo desasignamos

            // Creamos su movimiento de devolución
            Movimiento movDevolucion = new Movimiento();
            movDevolucion.setTipoDeMovimiento("Devolución por Reemplazo");
            movDevolucion.setFechaMovimiento(LocalDate.now());
            movDevolucion.setActivo(activoViejo);
            movDevolucion.setUsuario(usuario);
            movDevolucion.setUbicacion(ubicacionBodega); // ✅ Le asignamos la ubicación de Bodega
            movimientoRepository.save(movDevolucion);
        }

        // 3. Procesamos el ACTIVO NUEVO (lo asignamos al usuario)
        activoNuevo.setEstado(estadoEnUso);
        activoNuevo.setUsuarioActual(usuario);

        // Creamos su movimiento de asignación
        Movimiento movAsignacion = new Movimiento();
        movAsignacion.setTipoDeMovimiento(solicitud.getTipoSolicitud());
        movAsignacion.setFechaMovimiento(LocalDate.now());
        movAsignacion.setObservacion(request.getObservacion());
        movAsignacion.setActivo(activoNuevo);
        movAsignacion.setUsuario(usuario);
        movAsignacion.setUbicacion(ubicacionDestino); // Le asignamos la ubicación del formulario
        movimientoRepository.save(movAsignacion);

        // 4. Actualizamos la solicitud
        solicitud.setEstadoSolicitud("Procesado");
        solicitud.setMovimiento(movAsignacion);
        solicitudRepository.save(solicitud);

        enviarCorreoDeEntregaSiAplica(solicitud, activoNuevo, activoViejo);
    }

    private void enviarCorreoDeEntregaSiAplica(Solicitud solicitud, Activo activoEntregado, @Nullable Activo activoDevuelto) {
        System.out.println("--- INTENTANDO ENVIAR CORREO PARA TICKET: " + solicitud.getTicketId() + " ---");

        if (activoEntregado.getCategoria() == null || activoEntregado.getCategoria().getNombreCategoria() == null) {
            System.err.println("Correo no enviado: El activo entregado no tiene una categoría definida.");
            return;
        }

        String nombreCategoria = activoEntregado.getCategoria().getNombreCategoria();
        String tipoSolicitud = solicitud.getTipoSolicitud().toUpperCase();

        System.out.println("Datos para la validación del correo:");
        System.out.println("   > Categoría del activo: [" + nombreCategoria + "]");
        System.out.println("   > Tipo de solicitud: [" + tipoSolicitud + "]");

        boolean esTipoRelevante = tipoSolicitud.contains("DAÑO") || tipoSolicitud.contains("FALLO") || tipoSolicitud.contains("NUEVO");
        if (!esTipoRelevante) {
            System.out.println("CONDICIÓN FALLIDA: El tipo de solicitud no es relevante. Saliendo.");
            return;
        }

        System.out.println("Todas las condiciones cumplidas. Construyendo y enviando correo a: " + solicitud.getUsuario().getEmail());

        Activo activoConRelaciones = activoRepository.findById(activoEntregado.getIdEquipo())
                .orElseThrow(() -> new RuntimeException("Error al recargar el activo para enviar correo: " + activoEntregado.getIdEquipo()));

        Usuario usuario = solicitud.getUsuario();
        String asunto = "Entrega de Equipo - Ticket: " + solicitud.getTicketId();
        String cuerpoCorreo;

        String motivo = "Asignación de equipo nuevo solicitado.";
        if (activoDevuelto != null) {
            motivo = "El cambio se realiza por la solicitud: '" + solicitud.getTipoSolicitud() + "'.";
        }

        // ✅ --- LÓGICA DE CORREO MEJORADA ---
        // Se define la lista de alias para identificar un portátil.
        List<String> aliasDePortatil = List.of("PORTATIL", "PORTÁTIL", "LAPTOP");

        // SI es un portátil, usamos la plantilla especial que busca el cargador.
        if (aliasDePortatil.stream().anyMatch(alias -> alias.equalsIgnoreCase(nombreCategoria))) {

            String marcaPortatil = activoConRelaciones.getMarca() != null ? activoConRelaciones.getMarca() : "No especificada";
            String serialPortatil = activoConRelaciones.getNumeroDeSerie() != null ? activoConRelaciones.getNumeroDeSerie() : "No especificado";
            String marcaModeloCargador = "No enlazado";
            String serialCargador = "No enlazado";

            if (activoConRelaciones.getActivosRelacionados() != null) {
                for (Activo relacionado : activoConRelaciones.getActivosRelacionados()) {
                    if (relacionado.getCategoria() != null && "CARGADOR".equalsIgnoreCase(relacionado.getCategoria().getNombreCategoria())) {
                        String marca = relacionado.getMarca() != null ? relacionado.getMarca() : "";
                        String modelo = relacionado.getModelo() != null ? relacionado.getModelo() : "";
                        marcaModeloCargador = (marca + " " + modelo).trim();
                        serialCargador = relacionado.getNumeroDeSerie() != null ? relacionado.getNumeroDeSerie() : "No especificado";
                        break;
                    }
                }
            }

            cuerpoCorreo = String.format(
                    "Hola %s,\n\n" +
                            "Espero te encuentres muy bien,\n" +
                            "Con el presente correo hacemos entrega de un portátil con las siguientes características:\n\n" +
                            "Marca: %s\n" +
                            "Serial Portátil: %s\n" +
                            "Cargador: %s\n" +
                            "Serial cargador: %s\n\n" +
                            "%s\n\n" +
                            "Agradecemos confirmar la recepción de este correo como constancia de recibido.",
                    usuario.getNombre(),
                    marcaPortatil,
                    serialPortatil,
                    marcaModeloCargador,
                    serialCargador,
                    motivo
            );
        } else {
            // SI es cualquier otro activo, usamos una plantilla genérica.
            String marcaActivo = activoConRelaciones.getMarca() != null ? activoConRelaciones.getMarca() : "No especificada";
            String modeloActivo = activoConRelaciones.getModelo() != null ? activoConRelaciones.getModelo() : "";
            String serialActivo = activoConRelaciones.getNumeroDeSerie() != null ? activoConRelaciones.getNumeroDeSerie() : "No especificado";

            cuerpoCorreo = String.format(
                    "Hola %s,\n\n" +
                            "Espero te encuentres muy bien,\n" +
                            "Con el presente correo hacemos entrega de un %s con las siguientes características:\n\n" +
                            "Categoría: %s\n" +
                            "Marca: %s\n" +
                            "Modelo: %s\n" +
                            "Serial: %s\n\n" +
                            "%s\n\n" +
                            "Agradecemos confirmar la recepción de este correo como constancia de recibido.",
                    usuario.getNombre(),
                    nombreCategoria.toLowerCase(),
                    nombreCategoria,
                    marcaActivo,
                    modeloActivo,
                    serialActivo,
                    motivo
            );
        }
        // ✅ --- FIN DE LA MODIFICACIÓN ---

        emailService.sendSimpleEmail(usuario.getEmail(), asunto, cuerpoCorreo);
    }

    @Override
    public Optional<SolicitudDto> findSolicitudById(Long id) {
        return solicitudRepository.findById(id)
                .map(solicitudMapping::toDto);
    }

    @Override
    @Transactional
    public void enviarActivoAMantenimiento(Long solicitudId, Long activoId) {
        // 1. Buscamos la solicitud (sin cambios)
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // 2. Buscamos el activo específico por su ID
        Activo activo = activoRepository.findById(activoId)
                .orElseThrow(() -> new RuntimeException("Activo con ID " + activoId + " no encontrado."));

        // VERIFICACIÓN DE SEGURIDAD: Asegurarnos de que el activo pertenece al usuario de la solicitud
        if (!activo.getUsuarioActual().getIdUsuario().equals(solicitud.getUsuario().getIdUsuario())) {
            throw new SecurityException("Intento de procesar un activo que no pertenece al usuario.");
        }


        // 3. Buscamos el estado "En mantenimiento" (asumiendo que su ID es 4)
        Estado estadoMantenimiento = estadoRepository.findById(4L)
                .orElseThrow(() -> new RuntimeException("Estado 'En mantenimiento' no encontrado"));

        // 4. Actualizamos el activo: lo ponemos en mantenimiento y lo desasignamos
        activo.setEstado(estadoMantenimiento);
        activo.setUsuarioActual(null);

        // 5. Creamos el registro del movimiento
        Movimiento movimiento = new Movimiento();
        movimiento.setTipoDeMovimiento("Envío a Mantenimiento");
        movimiento.setFechaMovimiento(LocalDate.now());
        movimiento.setActivo(activo);
        movimiento.setUsuario(solicitud.getUsuario());

        // Asumimos que la ubicación para mantenimiento tiene el ID 2
        Ubicacion ubicacionMantenimiento = ubicacionRepository.findById(2L)
                .orElseThrow(() -> new RuntimeException("Ubicación de mantenimiento no encontrada"));
        movimiento.setUbicacion(ubicacionMantenimiento);

        movimientoRepository.save(movimiento);

        // 6. Actualizamos la solicitud para marcarla como "Procesada"
        solicitud.setEstadoSolicitud("Procesado");
        solicitud.setMovimiento(movimiento); // Enlazamos el movimiento a la solicitud
        solicitudRepository.save(solicitud);
    }

    @Override
    @Transactional
    public void procesarSolicitudDeDevolucion(Long solicitudId, DevolucionRequest request) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        Activo activoADevolver = activoRepository.findById(request.getActivoId())
                .orElseThrow(() -> new RuntimeException("Activo no encontrado"));

        // VALIDACIÓN CRÍTICA: ¿El activo pertenece al usuario de la solicitud?
        if (activoADevolver.getUsuarioActual() == null || !activoADevolver.getUsuarioActual().getIdUsuario().equals(solicitud.getUsuario().getIdUsuario())) {
            throw new IllegalStateException("El activo seleccionado no pertenece al usuario de la solicitud.");
        }

        // Actualizar el activo
        Estado estadoDisponible = estadoRepository.findById(6L).orElseThrow(() -> new RuntimeException("Estado 'Disponible' no encontrado"));
        activoADevolver.setUsuarioActual(null); // Desasignar
        activoADevolver.setEstado(estadoDisponible); // Poner en bodega

        // Crear el movimiento
        Movimiento movDevolucion = new Movimiento();
        movDevolucion.setTipoDeMovimiento("Devolución de Equipo");
        movDevolucion.setFechaMovimiento(LocalDate.now());
        movDevolucion.setActivo(activoADevolver);
        movDevolucion.setUsuario(solicitud.getUsuario());
        movDevolucion.setUbicacion(ubicacionRepository.findById(1L).get()); // A Bodega
        movDevolucion.setObservacion(request.getObservacion());
        movimientoRepository.save(movDevolucion);

        // Actualizar la solicitud
        solicitud.setEstadoSolicitud("Procesado");
        solicitud.setMovimiento(movDevolucion);
        solicitudRepository.save(solicitud);
    }

}
