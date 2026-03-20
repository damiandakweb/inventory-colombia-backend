package com.unicity.inventory.Service;

import com.unicity.inventory.Mapping.ActivoDto;
import com.unicity.inventory.Mapping.ActivoMapping;
import com.unicity.inventory.Models.*;
import com.unicity.inventory.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ActivoServiceImpl implements ActivoService {

    private final ActivoRepository activoRepository;
    private final CategoriaRepository categoriaRepository;
    private final EstadoRepository estadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ActivoMapping activoMapping;
    private final UbicacionRepository ubicacionRepository;
    private final MovimientoRepository movimientoRepository;

    @Override
    @Transactional(readOnly = true) // Añadir Transactional para asegurar que las relaciones LAZY funcionen
    public List<ActivoDto> getAllActivos() {
        // --- PASO 1: Obtener todos los activos en UNA sola consulta ---
        List<Activo> activos = activoRepository.findAll();

        if (activos.isEmpty()) {
            return Collections.emptyList();
        }

        // --- PASO 2: Obtener los ÚLTIMOS movimientos para TODOS esos activos en UNA sola consulta ---
        List<Long> activoIds = activos.stream().map(Activo::getIdEquipo).collect(Collectors.toList());
        List<Movimiento> ultimosMovimientos = movimientoRepository.findLatestMovementsForActivos(activoIds);

        // --- PASO 3: Crear un mapa para búsqueda instantánea (ID de Activo -> Movimiento) ---
        Map<Long, Movimiento> movimientoMap = ultimosMovimientos.stream()
                .collect(Collectors.toMap(mov -> mov.getActivo().getIdEquipo(), Function.identity()));

        // --- PASO 4: Mapear cada activo a su DTO y enriquecerlo con la ubicación del mapa ---
        return activos.stream().map(activo -> {
            // Mapeo base usando nuestro mapper "tonto"
            ActivoDto dto = activoMapping.toDto(activo);

            // Enriquecer el DTO con la información del mapa (esto es súper rápido)
            Movimiento ultimoMovimiento = movimientoMap.get(activo.getIdEquipo());
            if (ultimoMovimiento != null && ultimoMovimiento.getUbicacion() != null) {
                dto.setNombreUbicacionActual(ultimoMovimiento.getUbicacion().getNombreUbicacion());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public Optional<ActivoDto> findActivoById(Long id) {
        return activoRepository.findById(id).map(activoMapping::toDto);
    }

    @Override
    @Transactional
    public ActivoDto createActivo(ActivoDto dto) {
        Activo nuevoActivo = activoMapping.toEntity(dto);

        Categoria cat = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        Estado est = estadoRepository.findById(dto.getIdEstado())
                .orElseThrow(() -> new RuntimeException("Estado no encontrado"));

        nuevoActivo.setCategoria(cat);
        nuevoActivo.setEstado(est);

        // Guardamos el activo primero para que obtenga un ID
        Activo activoGuardado = activoRepository.save(nuevoActivo);

        // --- 👇 LÓGICA DEL PARCHE ACTUALIZADA ---
        Movimiento movimientoInicial = new Movimiento();
        movimientoInicial.setFechaMovimiento(LocalDate.now());
        movimientoInicial.setActivo(activoGuardado);

        // Asumimos que la ubicación de Bodega siempre tiene el ID 1
        Ubicacion ubicacionBodega = ubicacionRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Ubicación 'Bodega' con ID 1 no encontrada."));
        movimientoInicial.setUbicacion(ubicacionBodega);

        // Escenario 1: El activo SE ASIGNA a un usuario al crearse
        if (dto.getIdUsuarioActual() != null && dto.getIdUsuarioActual() > 0) {
            Usuario usuarioAsignado = usuarioRepository.findById(dto.getIdUsuarioActual())
                    .orElseThrow(() -> new RuntimeException("Usuario a asignar no encontrado"));

            activoGuardado.setUsuarioActual(usuarioAsignado);

            movimientoInicial.setTipoDeMovimiento("Asignación Inicial");
            movimientoInicial.setUsuario(usuarioAsignado);
            movimientoInicial.setObservacion("Movimiento generado automáticamente al crear y asignar el activo.");
        }
        // Escenario 2: El activo NO SE ASIGNA y va a bodega
        else {
            // Buscamos al usuario "Sistema" que creamos (con ID 99)
            Usuario usuarioSistema = usuarioRepository.findById(99L)
                    .orElseThrow(() -> new RuntimeException("Usuario 'Sistema' con ID 99 no encontrado."));

            movimientoInicial.setTipoDeMovimiento("Ingreso a Bodega");
            movimientoInicial.setUsuario(usuarioSistema);
            movimientoInicial.setObservacion("Movimiento generado automáticamente al crear el activo.");
        }

        movimientoRepository.save(movimientoInicial);
        // --- FIN DE LA LÓGICA DEL PARCHE ---

        return activoMapping.toDto(activoGuardado);
    }
    @Override
    @Transactional
    public Optional<ActivoDto> updateActivo(Long id, ActivoDto dto) {
        return activoRepository.findById(id).map(activoExistente -> {
            // Guardamos el ID del usuario anterior para comparar
            Long idUsuarioAnterior = (activoExistente.getUsuarioActual() != null) ? activoExistente.getUsuarioActual().getIdUsuario() : null;

            // Buscamos las entidades relacionadas
            Categoria cat = categoriaRepository.findById(dto.getIdCategoria()).orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            Estado est = estadoRepository.findById(dto.getIdEstado()).orElseThrow(() -> new RuntimeException("Estado no encontrado"));

            // Actualizamos los campos del activo
            activoExistente.setNumeroDeSerie(dto.getNumeroDeSerie());
            activoExistente.setEtiquetaInventario(dto.getEtiquetaInventario());

            // 👇 --- SOLUCIÓN: AÑADE ESTAS LÍNEAS ---
            // Asigna las fechas desde el DTO a la entidad existente.
            // (Asegúrate de que los nombres de los métodos get sean correctos según tu ActivoDto)
            activoExistente.setFechaCompra(dto.getFechaCompra());
            activoExistente.setFechaGarantia(dto.getFechaGarantia());
            activoExistente.setMarca(dto.getMarca());
            activoExistente.setModelo(dto.getModelo());

            activoExistente.setPais(dto.getPais());

            activoExistente.setCategoria(cat);
            activoExistente.setEstado(est);

            Usuario usuarioNuevo = null;
            if (dto.getIdUsuarioActual() != null && dto.getIdUsuarioActual() > 0) {
                usuarioNuevo = usuarioRepository.findById(dto.getIdUsuarioActual()).orElse(null);
            }
            activoExistente.setUsuarioActual(usuarioNuevo);

            // LÓGICA DE MOVIMIENTO AUTOMÁTICO
            if (idUsuarioAnterior != dto.getIdUsuarioActual()) {
                Movimiento movimiento = new Movimiento();
                movimiento.setFechaMovimiento(LocalDate.now());
                movimiento.setActivo(activoExistente);

                if (usuarioNuevo != null) {
                    movimiento.setTipoDeMovimiento("Asignación Directa");
                    movimiento.setUsuario(usuarioNuevo);
                } else {
                    movimiento.setTipoDeMovimiento("Devolución a Bodega");
                    Usuario usuarioAnteriorObj = (idUsuarioAnterior != null) ? usuarioRepository.findById(idUsuarioAnterior).orElse(null) : null;
                    movimiento.setUsuario(usuarioAnteriorObj);
                }
                Ubicacion ubicacion = ubicacionRepository.findById(1L).orElseThrow(() -> new RuntimeException("Ubicación por defecto no encontrada"));
                movimiento.setUbicacion(ubicacion);
                movimientoRepository.save(movimiento);
            }

            return activoMapping.toDto(activoRepository.save(activoExistente));
        });
    }

    @Override
    @Transactional
    public void deleteActivo(Long id) {
        // 1. Buscamos el activo que se va a dar de baja.
        Activo activo = activoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activo no encontrado con ID: " + id));

        // 2. Buscamos las entidades necesarias para el movimiento.
        Estado estadoBaja = estadoRepository.findById(5L) // Asumiendo que el ID 5 es "Baja" o "Retirado"
                .orElseThrow(() -> new RuntimeException("Estado 'Baja' con ID 5 no encontrado."));
        Ubicacion ubicacionBodega = ubicacionRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Ubicación 'Bodega' con ID 1 no encontrada."));

        // --- LÓGICA CORREGIDA ---
        // 3. Determinamos quién realiza el movimiento.
        Usuario usuarioDelMovimiento;

        // Si el activo tenía un usuario asignado, ese es el usuario del movimiento.
        if (activo.getUsuarioActual() != null) {
            usuarioDelMovimiento = activo.getUsuarioActual();
        } else {
            // Si el activo estaba en bodega (sin usuario), el movimiento se registra a nombre del "Sistema".
            usuarioDelMovimiento = usuarioRepository.findById(99L)
                    .orElseThrow(() -> new RuntimeException("Usuario 'Sistema' con ID 99 no encontrado."));
        }

        // 4. Creamos el movimiento de "Baja".
        Movimiento movimientoDeBaja = new Movimiento();
        movimientoDeBaja.setTipoDeMovimiento("Baja de Activo");
        movimientoDeBaja.setFechaMovimiento(LocalDate.now());
        movimientoDeBaja.setActivo(activo);
        movimientoDeBaja.setUsuario(usuarioDelMovimiento); // ✅ Asignamos el usuario correcto.
        movimientoDeBaja.setUbicacion(ubicacionBodega);
        movimientoDeBaja.setObservacion("Activo dado de baja del sistema.");
        movimientoRepository.save(movimientoDeBaja);

        // 5. Actualizamos el estado final del activo.
        activo.setEstado(estadoBaja);
        activo.setUsuarioActual(null); // Nos aseguramos de que no quede asignado a nadie.
        activoRepository.save(activo);

        // Nota: En lugar de borrar el activo, lo marcamos como "Baja". Si realmente quieres borrarlo,
        // la línea sería activoRepository.delete(activo); pero se perdería el historial.
    }
    @Override
    public List<ActivoDto> findActivoByUsuarioAndCategoria(Long usuarioId, Long categoriaId) {
        // 1. Llama al repositorio para obtener la lista de entidades Activo
        return activoRepository.findActivoByUsuarioAndCategoria(usuarioId, categoriaId)
                .stream() // 2. Convierte la lista a un Stream
                .map(activoMapping::toDto) // 3. Mapea cada Activo a un ActivoDto
                .collect(Collectors.toList()); // 4. Colecciona los resultados en una nueva lista
    }

    @Override
    public List<ActivoDto> findDisponiblesByCategoria(Long categoriaId) {
        // ✅ ESTA LÍNEA DEFINE TU REQUERIMIENTO
        // Le decimos al sistema que "disponible" significa estado 1 (Nuevo) O estado 6 (En Bodega).
        List<Long> estadosDisponiblesIds = Arrays.asList(1L, 6L);

        // El resto del código usa esa lista para buscar en la base de datos
        List<Activo> activosDisponibles = activoRepository.findActivosDisponiblesPorCategoriaYEstados(
                categoriaId,
                estadosDisponiblesIds
        );

        // Mapea los resultados a DTOs como antes.
        return activosDisponibles.stream()
                .map(activoMapping::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ActivoDto> getActivosByUsuarioId(Long usuarioId) {
        // 1. Llama al repositorio para obtener las entidades de Activo
        return activoRepository.findActivosByUsuarioId(usuarioId)
                .stream()
                // 2. Mapea cada entidad a un DTO
                .map(activoMapping::toDto)
                // 3. Colecciona los resultados en una lista de DTOs
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void enlazarActivo(Long activoId, Long relacionadoId) {
        Activo activoPrincipal = activoRepository.findById(activoId)
                .orElseThrow(() -> new RuntimeException("Activo principal no encontrado con ID: " + activoId));
        Activo activoRelacionado = activoRepository.findById(relacionadoId)
                .orElseThrow(() -> new RuntimeException("Activo a relacionar no encontrado con ID: " + relacionadoId));

        // Añadimos la relación en ambas direcciones
        activoPrincipal.getActivosRelacionados().add(activoRelacionado);
        activoRelacionado.getActivosRelacionados().add(activoPrincipal);

        // Guardamos la entidad principal. Ahora, gracias a la cascada,
        // JPA guardará los cambios en la tabla de unión.
        activoRepository.save(activoPrincipal);
    }

    @Override
    @Transactional
    public void desenlazarActivo(Long activoId, Long relacionadoId) {
        Activo activoPrincipal = activoRepository.findById(activoId)
                .orElseThrow(() -> new RuntimeException("Activo principal no encontrado con ID: " + activoId));
        Activo activoRelacionado = activoRepository.findById(relacionadoId)
                .orElseThrow(() -> new RuntimeException("Activo a desenlazar no encontrado con ID: " + relacionadoId));

        // Eliminamos la relación en ambas direcciones
        activoPrincipal.getActivosRelacionados().remove(activoRelacionado);
        activoRelacionado.getActivosRelacionados().remove(activoPrincipal);

        activoRepository.save(activoPrincipal);
    }

    @Override
    @Transactional(readOnly = true) // Necesario para cargar relaciones LAZY
    public List<ActivoDto> findActivosRelacionados(Long activoId) {
        // 1. Busca el activo principal por su ID
        Activo activoPrincipal = activoRepository.findById(activoId)
                .orElseThrow(() -> new RuntimeException("Activo no encontrado con ID: " + activoId));

        // 2. Accede a la colección de relacionados (gracias a @Transactional, se cargará ahora)
        //    y mapea cada uno usando el mapper simple para evitar bucles.
        if (activoPrincipal.getActivosRelacionados() != null) {
            return activoPrincipal.getActivosRelacionados().stream()
                    .map(activoMapping::toSimpleDto) // Usamos toSimpleDto
                    .collect(Collectors.toList());
        } else {
            // Si por alguna razón la colección es nula, devuelve una lista vacía
            return Collections.emptyList();
        }
    }
}