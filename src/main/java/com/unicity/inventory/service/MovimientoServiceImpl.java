package com.unicity.inventory.service;

import com.unicity.inventory.mapping.MovimientoDto;
import com.unicity.inventory.mapping.MovimientoMapping;
import com.unicity.inventory.models.*;
import com.unicity.inventory.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.unicity.inventory.exceptions.ResourceNotFoundException;
import com.unicity.inventory.exceptions.BusinessException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovimientoServiceImpl implements MovimientoService {


    private final MovimientoRepository movimientoRepository;
    private final MovimientoMapping movimientoMapping;
    private final ActivoRepository activoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UbicacionRepository ubicacionRepository;
    private final EstadoServicelmpl estadoServicelmpl;
    private final EstadoRepository estadoRepository;


    @Override
    public boolean existByIdMovimiento(Long id) {
        return movimientoRepository.existsById(id);
    }

    @Override
    @Transactional
    public MovimientoDto createMovimiento(MovimientoDto dto) {
        // 1. Buscamos las entidades principales
        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + dto.getIdUsuario()));
        Ubicacion ubicacion = ubicacionRepository.findById(dto.getIdUbicacion())
                .orElseThrow(() -> new ResourceNotFoundException("Ubicación no encontrada con ID: " + dto.getIdUbicacion()));
        Activo activo = activoRepository.findById(dto.getIdEquipo())
                .orElseThrow(() -> new ResourceNotFoundException("Activo no encontrado con ID: " + dto.getIdEquipo()));

        // 2. Lógica para determinar y actualizar el estado del activo
        String tipoMovimiento = dto.getTipoDeMovimiento().trim().toUpperCase();
        Estado estadoNuevo;

        switch (tipoMovimiento) {
            case "INGRESO":
                estadoNuevo = estadoRepository.findByNombreEstado("Nuevo")
                        .orElseThrow(() -> new ResourceNotFoundException("Estado 'Nuevo' no encontrado"));
                activo.setUsuarioActual(null);
                break;

            case "DEVOLUCION DE EQUIPO":
                boolean esBodega = ubicacion.getNombreUbicacion().equalsIgnoreCase("Bodega");
                boolean esSistema = usuario.getIdUsuario().equals(99L);
                String estadoActualNombre = activo.getEstado().getNombreEstado().toUpperCase();

                if (esBodega) {
                    // FLUJO A: Devolución real → activo va a bodega
                    // Validación: si ya está en bodega no tiene sentido
                    if (estadoActualNombre.contains("BODEGA") && activo.getUsuarioActual() == null) {
                        throw new IllegalStateException("El activo '" + activo.getEtiquetaInventario() + "' ya está en bodega.");
                    }
                    // Validación: solo el dueño o Sistema puede devolver
                    if (!esSistema && activo.getUsuarioActual() != null &&
                            !activo.getUsuarioActual().getIdUsuario().equals(usuario.getIdUsuario())) {
                        throw new BusinessException("El activo pertenece a '" +
                                activo.getUsuarioActual().getNombre() + "', no a '" + usuario.getNombre() + "'.");
                    }
                    estadoNuevo = estadoRepository.findByNombreEstado("En bodega")
                            .orElseThrow(() -> new ResourceNotFoundException("Estado 'En bodega' no encontrado"));
                    activo.setUsuarioActual(null);

                } else {
                    // FLUJO B: Entrega/regreso → activo sale de bodega o mantenimiento hacia un usuario
                    // Validación: el activo debe estar disponible (bodega, mantenimiento, dañado en bodega)
                    boolean estaDisponible = estadoActualNombre.contains("BODEGA") ||
                            estadoActualNombre.contains("MANTENIMIENTO") ||
                            estadoActualNombre.contains("NUEVO");
                    if (!estaDisponible) {
                        throw new IllegalStateException("El activo '" + activo.getEtiquetaInventario() +
                                "' está en estado '" + activo.getEstado().getNombreEstado() +
                                "'. Solo se pueden entregar activos en bodega o mantenimiento.");
                    }
                    estadoNuevo = estadoRepository.findByNombreEstado("En uso")
                            .orElseThrow(() -> new ResourceNotFoundException("Estado 'En uso' no encontrado"));
                    activo.setUsuarioActual(usuario);
                }
                break;
            case "ASIGNACION":
            case "CAMBIO POR DAÑO":
            case "CAMBIO POR FALLO":
            case "SOLICITUD DE EQUIPO NUEVO":
                if (activo.getUsuarioActual() != null &&
                        !activo.getUsuarioActual().getIdUsuario().equals(usuario.getIdUsuario())) {
                    Movimiento movDevolucionAuto = new Movimiento();
                    movDevolucionAuto.setTipoDeMovimiento("Devolución Automática");
                    movDevolucionAuto.setFechaMovimiento(LocalDate.now());
                    movDevolucionAuto.setActivo(activo);
                    movDevolucionAuto.setUsuario(activo.getUsuarioActual());
                    movDevolucionAuto.setUbicacion(ubicacion);
                    movDevolucionAuto.setObservacion("Devolución automática generada al reasignar el activo.");
                    movimientoRepository.save(movDevolucionAuto);
                }
                // La ubicación determina el estado
                estadoNuevo = ubicacion.getNombreUbicacion().equalsIgnoreCase("Bodega")
                        ? estadoRepository.findByNombreEstado("En bodega")
                        .orElseThrow(() -> new ResourceNotFoundException("Estado 'En bodega' no encontrado"))
                        : estadoRepository.findByNombreEstado("En uso")
                        .orElseThrow(() -> new ResourceNotFoundException("Estado 'En uso' no encontrado"));
                activo.setUsuarioActual(ubicacion.getNombreUbicacion().equalsIgnoreCase("Bodega") ? null : usuario);
                break;

            case "MANTENIMIENTO":
                estadoNuevo = estadoRepository.findByNombreEstado("Mantenimiento")
                        .orElseThrow(() -> new ResourceNotFoundException("Estado 'Mantenimiento' no encontrado"));
                activo.setUsuarioActual(null);
                break;

            case "TRASLADO":
                // Traslado también usa la lógica de ubicación
                estadoNuevo = ubicacion.getNombreUbicacion().equalsIgnoreCase("Bodega")
                        ? estadoRepository.findByNombreEstado("En bodega")
                        .orElseThrow(() -> new ResourceNotFoundException("Estado 'En bodega' no encontrado"))
                        : activo.getEstado();
                if (ubicacion.getNombreUbicacion().equalsIgnoreCase("Bodega")) {
                    activo.setUsuarioActual(null);
                }
                break;

            default:
                throw new IllegalArgumentException("Tipo de movimiento no reconocido: '" + dto.getTipoDeMovimiento() + "'. Use el botón de baja para retirar activos.");
        }

        activo.setEstado(estadoNuevo);

        // 3. Crear y guardar el nuevo registro de movimiento
        Movimiento nuevoMovimiento = new Movimiento();
        nuevoMovimiento.setTipoDeMovimiento(dto.getTipoDeMovimiento());
        nuevoMovimiento.setObservacion(dto.getObservacion());
        nuevoMovimiento.setFechaMovimiento(LocalDate.now());
        nuevoMovimiento.setActivo(activo);
        nuevoMovimiento.setUsuario(usuario);
        nuevoMovimiento.setUbicacion(ubicacion);
        movimientoRepository.save(nuevoMovimiento);

        return movimientoMapping.movimientoToDto(nuevoMovimiento);
    }

    @Override
    public boolean deleteMovimiento(Long id) {
        if (movimientoRepository.existsById(id)) {
            movimientoRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<Movimiento> getAllMovimientos() {
        return movimientoRepository.findAll();
    }


    @Override
    public List<MovimientoDto> getMovimientosByActivoId(Long activoId) {
        // 1. Llama al repositorio para obtener las entidades
        return movimientoRepository.findMovimientosByActivoId(activoId)
                .stream()
                // 2. Mapea cada entidad a un DTO
                .map(movimientoMapping::movimientoToDto)
                // 3. Colecciona los resultados en una lista
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Movimiento> findMovimientoById(Long id) {
        return movimientoRepository.findById(id);
    }

    @Override
    public Optional<MovimientoDto> updateMovimiento(Long id, MovimientoDto movimientoDetails) {

        Optional<Movimiento> optionalMovimiento = movimientoRepository.findById(id);

        if (optionalMovimiento.isPresent()) {
            Movimiento movimiento = optionalMovimiento.get();

            // Buscar relaciones


            Activo activo = activoRepository.findById(movimientoDetails.getIdEquipo())
                    .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado"));

            Usuario usuario = usuarioRepository.findById(movimientoDetails.getIdUsuario())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

            Ubicacion ubicacion = ubicacionRepository.findById(movimientoDetails.getIdUbicacion())
                    .orElseThrow(() -> new ResourceNotFoundException("Ubicacion no encontrada"));

            // Actualizar campos

            movimiento.setTipoDeMovimiento(movimientoDetails.getTipoDeMovimiento());
            movimiento.setFechaMovimiento(movimientoDetails.getFechaMovimiento());
            movimiento.setObservacion(movimientoDetails.getObservacion());
            movimiento.setActivo(activo);
            movimiento.setUsuario(usuario);
            movimiento.setUbicacion(ubicacion);

            // Guardar y devolver resultado
            Movimiento actualizado = movimientoRepository.save(movimiento);
            MovimientoDto resultDto = movimientoMapping.movimientoToDto(actualizado);
            return Optional.of(resultDto);
        } else {
            return Optional.empty();
        }
    }
}
