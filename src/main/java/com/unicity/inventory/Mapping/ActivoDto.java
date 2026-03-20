package com.unicity.inventory.Mapping;

import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ActivoDto {
    private Long idEquipo;
    private String numeroDeSerie;
    private LocalDate fechaGarantia;
    private LocalDate fechaCompra;
    private String etiquetaInventario;
    private Long idEstado;
    private Long idCategoria;
    private Long idUsuarioActual;
    private String nombreEstado;
    private String nombreCategoria;
    private String nombreUsuarioActual;
    private String nombreUbicacionActual;
    private String marca;
    private String modelo;
    private String pais;
    private List<ActivoDto> activosRelacionados = new ArrayList<>();
}
