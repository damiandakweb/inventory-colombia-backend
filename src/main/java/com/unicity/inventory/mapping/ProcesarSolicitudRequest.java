package com.unicity.inventory.mapping;
import lombok.Data;
@Data
public class ProcesarSolicitudRequest {
    private Long idActivoNuevo;
    private Long idActivoViejo;
    private Long idUbicacion;
    private String observacion;
}