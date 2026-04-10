package com.unicity.inventory.service;

import com.unicity.inventory.models.Activo;
import com.unicity.inventory.repository.ActivoRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@EnableScheduling
public class SyncScheduler {

    private final ITGlueService itGlueService;
    private final SyncService syncService;
    private final ActivoRepository activoRepository;

    public SyncScheduler(ITGlueService itGlueService,
                         SyncService syncService,
                         ActivoRepository activoRepository) {
        this.itGlueService = itGlueService;
        this.syncService = syncService;
        this.activoRepository = activoRepository;
    }

    // ✅ Refresca cache de tipos ITGlue al arrancar (5s de delay)
    @Scheduled(initialDelay = 5000, fixedRate = 86400000)
    public void refreshOnStartup() {
        System.out.println("=== Cargando tipos ITGlue al arrancar...");
        itGlueService.refreshTypeCache();
        System.out.println("=== Cache ITGlue listo.");
    }

    // ✅ Sync completo cada 24h (arranca 1 minuto después del inicio)
    @Scheduled(initialDelay = 60000, fixedRate = 86400000)
    public void syncAllActivos() {
        List<Activo> activos = activoRepository.findAll();

        System.out.println("=== [SCHEDULER] Iniciando sync diario de "
                + activos.size() + " activos ===");

        AtomicInteger ok = new AtomicInteger(0);
        AtomicInteger errores = new AtomicInteger(0);

        for (Activo activo : activos) {
            try {
                syncService.syncActivo(activo);
                ok.incrementAndGet();
                // Pausa de 500ms para no saturar las APIs
                Thread.sleep(500);
            } catch (Exception e) {
                errores.incrementAndGet();
                System.err.println("Error sync activo "
                        + activo.getIdEquipo() + ": " + e.getMessage());
            }
        }

        System.out.println("=== [SCHEDULER] Sync diario completado. OK: "
                + ok.get() + " | Errores: " + errores.get() + " ===");
    }
}