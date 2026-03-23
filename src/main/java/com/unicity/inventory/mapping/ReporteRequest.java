package com.unicity.inventory.mapping;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ReporteRequest {
    // Una lista con los nombres de las columnas que el usuario quiere en el reporte
    // ej: ["etiquetaInventario", "nombreCategoria", "nombreUsuarioActual"]
    private List<String> columnas;

    // Un mapa con los filtros que el usuario aplicó
    // ej: {"idCategoria": 5, "idEstado": 6}
    private Map<String, Object> filtros;
}