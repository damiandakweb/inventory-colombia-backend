package com.unicity.inventory.tasks;

import com.unicity.inventory.Mapping.SolicitudDto;
import com.unicity.inventory.Service.CsvReaderService; // Importamos el nuevo servicio
import com.unicity.inventory.Service.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SolicitudScheduler {

    private final CsvReaderService csvReaderService;
    private final SolicitudService solicitudService;

    @Scheduled(fixedRate = 60000) // Se ejecuta cada 5 minutos
    public void checkForNewSolicitudes() {
        System.out.println("Buscando nuevas solicitudes en Google Sheets...");

        List<SolicitudDto> solicitudesFromCsv = csvReaderService.leerSolicitudesDesdeCsv();

        // Esta lógica previene duplicados. Tu SolicitudService debe implementarla.
        for (SolicitudDto dto : solicitudesFromCsv) {
            try {
                // El servicio verificará si ya existe antes de crearla
                solicitudService.crearNuevaSolicitud(dto);
            } catch (Exception e) {
                System.out.println("Error al procesar solicitud para " + dto.getNombreUsuario() + ": " + e.getMessage());
            }
        }
    }
}