package com.unicity.inventory.Service;

import com.unicity.inventory.Mapping.ReporteRequest;

public interface ReporteService {

    /**
     * Genera un archivo CSV basado en las columnas y filtros
     * especificados en el objeto ReporteRequest.
     *
     * @param request El objeto que contiene las columnas y filtros deseados.
     * @return Un String que representa el contenido del archivo CSV.
     */
    String generarCsvDinamico(ReporteRequest request);

}