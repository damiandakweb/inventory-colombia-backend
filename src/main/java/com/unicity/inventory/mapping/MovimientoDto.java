package com.unicity.inventory.mapping;



import lombok.*;

import java.time.LocalDate;


@Data
public class MovimientoDto {

    private Long idMovimiento;
    private String tipoDeMovimiento;
    private LocalDate fechaMovimiento;
    private String observacion;

    // Llaves foraneas
    private Long idEquipo;
    private Long idUsuario;
    private Long idUbicacion;

    //Nombres para poder mostrar en el frontend
    private String etiquetaActivo;
    private String nombreUsuario;
    private String nombreUbicacion;


}
