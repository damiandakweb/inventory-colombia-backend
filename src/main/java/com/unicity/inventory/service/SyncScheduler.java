package com.unicity.inventory.service;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class SyncScheduler {

    private final ITGlueService itGlueService;

    public SyncScheduler(ITGlueService itGlueService) {
        this.itGlueService = itGlueService;
    }

    // ✅ Refresca el cache de tipos cada 24 horas
    @Scheduled(fixedRate = 86400000)
    public void refreshITGlueCache() {
        System.out.println("Refrescando cache de tipos ITGlue...");
        itGlueService.refreshTypeCache();
        System.out.println("Cache de tipos ITGlue actualizado.");
    }

    // ✅ También refresca al arrancar la aplicación
    @Scheduled(initialDelay = 5000, fixedRate = 86400000)
    public void refreshOnStartup() {
        System.out.println("Cargando tipos ITGlue al arrancar...");
        itGlueService.refreshTypeCache();
    }
}