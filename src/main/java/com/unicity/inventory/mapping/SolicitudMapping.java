package com.unicity.inventory.mapping;

import com.unicity.inventory.models.Solicitud;
import org.springframework.stereotype.Component;

@Component
public class SolicitudMapping {

    public SolicitudDto toDto(Solicitud solicitud) {
        if (solicitud == null) return null;

        SolicitudDto dto = new SolicitudDto();

        // Asignamos todos los campos necesarios
        dto.setIdSolicitud(solicitud.getIdSolicitud());
        dto.setTipoSolicitud(solicitud.getTipoSolicitud());
        dto.setFechaSolicitud(solicitud.getFechaSolicitud());
        dto.setEstadoSolicitud(solicitud.getEstadoSolicitud());
        dto.setTicketId(solicitud.getTicketId());

        if (solicitud.getUsuario() != null) {
            dto.setNombreUsuario(solicitud.getUsuario().getNombre());
            dto.setIdUsuario(solicitud.getUsuario().getIdUsuario());
        }
        if (solicitud.getCategoria() != null) {
            dto.setNombreCategoria(solicitud.getCategoria().getNombreCategoria());
            dto.setIdCategoria(solicitud.getCategoria().getIdCategoria());
        }

        return dto;
    }
}