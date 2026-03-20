package com.unicity.inventory.Mapping;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SolicitudDto {
    // Campos que necesita la tabla en el frontend
    private Long idSolicitud;
    private String tipoSolicitud;
    private LocalDate fechaSolicitud;
    private String estadoSolicitud;

    private Long idUsuario;
    private String nombreUsuario;
    private String nombreCategoria;
    private Long idCategoria;
    private String marcaTemporalFuente;
    private String ticketId;

}