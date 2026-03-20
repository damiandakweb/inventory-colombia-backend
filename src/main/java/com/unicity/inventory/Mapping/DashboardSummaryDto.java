package com.unicity.inventory.Mapping;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardSummaryDto {
    // Para las tarjetas de estadísticas
    private long totalActivos;
    private long totalUsuarios;
    private long solicitudesPendientes; // Lo dejaremos en 0 por ahora

    // Para el gráfico de torta
    private List<Map<String, Object>> activosPorEstado;

    // Para la lista de últimos movimientos
    private List<MovimientoDto> ultimosMovimientos;

    private long computadoresBackup;
}