package com.unicity.inventory.controller;

import com.unicity.inventory.models.Activo;
import com.unicity.inventory.repository.ActivoRepository;
import com.unicity.inventory.service.SyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncService syncService;
    private final ActivoRepository activoRepository;

    public SyncController(SyncService syncService, ActivoRepository activoRepository) {
        this.syncService = syncService;
        this.activoRepository = activoRepository;
    }

    // ✅ Sincroniza todos los activos que no tienen itglue_id
    @PostMapping("/itglue/full")
    public ResponseEntity<String> syncAllToITGlue() {
        List<Activo> activos = activoRepository.findAll();

        System.out.println("=== Iniciando sincronización masiva de "
                + activos.size() + " activos ===");

        AtomicInteger sincronizados = new AtomicInteger(0);
        AtomicInteger errores = new AtomicInteger(0);

        for (Activo activo : activos) {
            try {
                syncService.syncActivo(activo);
                sincronizados.incrementAndGet();
                System.out.println("✓ Sincronizado " + sincronizados.get()
                        + "/" + activos.size() + " - " + activo.getEtiquetaInventario());
                Thread.sleep(500);
            } catch (Exception e) {
                errores.incrementAndGet();
                System.err.println("✗ Error en activo "
                        + activo.getIdEquipo() + ": " + e.getMessage());
            }
        }

        System.out.println("=== Sincronización completada. Sincronizados: "
                + sincronizados.get() + ", Errores: " + errores.get() + " ===");

        return ResponseEntity.ok("Sincronización completada. Sincronizados: "
                + sincronizados.get() + ", Errores: " + errores.get());
    }

    // ✅ Sincroniza un activo específico por ID
    @PostMapping("/itglue/{id}")
    public ResponseEntity<String> syncOneToITGlue(@PathVariable Long id) {
        return activoRepository.findById(id)
                .map(activo -> {
                    syncService.syncActivo(activo);
                    return ResponseEntity.ok("Activo " + id + " sincronizado correctamente.");
                })
                .orElse(ResponseEntity.notFound().build());
    }
}