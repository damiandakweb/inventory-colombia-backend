package com.unicity.inventory.Service;

import com.unicity.inventory.Mapping.ActivoMapping;
import com.unicity.inventory.Mapping.MovimientoDto;
import com.unicity.inventory.Mapping.MovimientoMapping;
import com.unicity.inventory.Models.*;
import com.unicity.inventory.Repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + dto.getIdUsuario()));
        Ubicacion ubicacion = ubicacionRepository.findById(dto.getIdUbicacion())
                .orElseThrow(() -> new RuntimeException("Ubicación no encontrada con ID: " + dto.getIdUbicacion()));
        Activo activo = activoRepository.findById(dto.getIdEquipo())
                .orElseThrow(() -> new RuntimeException("Activo no encontrado con ID: " + dto.getIdEquipo()));

        // 2. Lógica para determinar y actualizar el estado del activo
        String tipoMovimiento = dto.getTipoDeMovimiento().trim().toUpperCase();
        Estado estadoNuevo;

        switch (tipoMovimiento) {
            case "INGRESO":
                // La lógica de ingreso puede permanecer simple
                estadoNuevo = estadoRepository.findById(1L) // Estado: Disponible
                        .orElseThrow(() -> new RuntimeException("Estado 'Disponible' no encontrado"));
                activo.setUsuarioActual(null);
                break;

            case "DEVOLUCION DE EQUIPO":
                // --- 👇 INICIO DE LA LÓGICA INTELIGENTE ---

                // Validación 1: ¿El activo está asignado a ALGUIEN?
                if (activo.getUsuarioActual() == null) {
                    throw new IllegalStateException("Error: El activo '" + activo.getEtiquetaInventario() + "' ya se encuentra en bodega y no puede ser devuelto.");
                }

                // Validación 2: ¿El activo pertenece al usuario que intenta devolverlo?
                if (!activo.getUsuarioActual().getIdUsuario().equals(usuario.getIdUsuario())) {
                    throw new SecurityException("Error de asignación: El activo '" + activo.getEtiquetaInventario() + "' pertenece a '" + activo.getUsuarioActual().getNombre() + "', no a '" + usuario.getNombre() + "'.");
                }

                // Validación 3 (Opcional pero recomendada): ¿El activo está 'En Uso'?
                // Asumiendo que el ID 2L es 'En Uso'.
                if (activo.getEstado().getIdEstado() != 2L) {
                    throw new IllegalStateException("Error: Solo se pueden devolver activos que estén 'En Uso'. Estado actual: " + activo.getEstado().getNombreEstado());
                }

                // Si todas las validaciones pasan, procedemos.
                estadoNuevo = estadoRepository.findById(1L) // Estado: Disponible
                        .orElseThrow(() -> new RuntimeException("Estado 'Disponible' no encontrado"));
                activo.setUsuarioActual(null); // Desasignamos el activo del usuario

                // --- FIN DE LA LÓGICA INTELIGENTE ---
                break;

            case "ASIGNACION":
            case "CAMBIO POR DAÑO":
            case "CAMBIO POR FALLO":
            case "SOLICITUD DE EQUIPO NUEVO":
                // Validación: No se puede asignar un activo que ya está en uso por otra persona.
                if (activo.getUsuarioActual() != null && !activo.getUsuarioActual().getIdUsuario().equals(usuario.getIdUsuario())) {
                    throw new IllegalStateException("Error: El activo '" + activo.getEtiquetaInventario() + "' ya está asignado a " + activo.getUsuarioActual().getNombre() + ".");
                }
                estadoNuevo = estadoRepository.findById(2L) // Estado: En uso
                        .orElseThrow(() -> new RuntimeException("Estado 'En uso' no encontrado"));
                activo.setUsuarioActual(usuario);
                break;

            // ... (resto de los 'case' sin cambios)
            case "MANTENIMIENTO":
                estadoNuevo = estadoRepository.findById(4L) // Estado: En mantenimiento
                        .orElseThrow(() -> new RuntimeException("Estado 'En mantenimiento' no encontrado"));
                activo.setUsuarioActual(null);
                break;

            case "BAJA":
                estadoNuevo = estadoRepository.findById(5L) // Estado: Retirado
                        .orElseThrow(() -> new RuntimeException("Estado 'Retirado' no encontrado"));
                activo.setUsuarioActual(null);
                break;

            case "TRASLADO":
                estadoNuevo = activo.getEstado();
                break;

            default:
                throw new IllegalArgumentException("Tipo de movimiento no válido: " + dto.getTipoDeMovimiento());
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
                    .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

            Usuario usuario = usuarioRepository.findById(movimientoDetails.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Ubicacion ubicacion = ubicacionRepository.findById(movimientoDetails.getIdUbicacion())
                    .orElseThrow(() -> new RuntimeException("Ubicacion no encontrada"));

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
