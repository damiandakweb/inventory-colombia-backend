package com.unicity.inventory.Mapping;


import com.unicity.inventory.Models.Activo;
import com.unicity.inventory.Models.Movimiento;
import org.springframework.stereotype.Component;

@Component
public class MovimientoMapping {

    public MovimientoDto movimientoToDto(Movimiento movimiento) {

        if (movimiento == null) {
            return null;
        }
        MovimientoDto dto = new MovimientoDto();
        dto.setIdMovimiento(movimiento.getIdMovimiento());
        dto.setTipoDeMovimiento(movimiento.getTipoDeMovimiento());
        dto.setFechaMovimiento(movimiento.getFechaMovimiento());
        dto.setObservacion(movimiento.getObservacion());

        // Asignamos los IDs de las entidades relacionadas para no enviar los objetos completos
        if (movimiento.getActivo() != null) {
            dto.setIdEquipo(movimiento.getActivo().getIdEquipo());
            dto.setEtiquetaActivo(movimiento.getActivo().getEtiquetaInventario()); // Obtenemos la etiqueta
        }
        if (movimiento.getUsuario() != null) {
            dto.setIdUsuario(movimiento.getUsuario().getIdUsuario());

            dto.setNombreUsuario(movimiento.getUsuario().getNombre()); // Obtenemos el nombre
        }
        if (movimiento.getUbicacion() != null) {
            dto.setIdUbicacion(movimiento.getUbicacion().getIdUbicacion());

            dto.setNombreUbicacion(movimiento.getUbicacion().getNombreUbicacion()); // Obtenemos el nombre
        }

        return dto;
    }
}
