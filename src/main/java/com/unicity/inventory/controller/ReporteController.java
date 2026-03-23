package com.unicity.inventory.controller;

import com.unicity.inventory.mapping.ReporteRequest;
import com.unicity.inventory.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @PostMapping(value = "/dinamico", produces = "text/csv")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<String> generarReporteDinamico(@RequestBody ReporteRequest request) {

        String csvData = reporteService.generarCsvDinamico(request);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_personalizado.csv");

        return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
    }
}
