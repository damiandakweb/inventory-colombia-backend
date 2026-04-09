package com.unicity.inventory.service;

import com.unicity.inventory.models.Activo;
import com.unicity.inventory.models.Movimiento;
import com.unicity.inventory.repository.ActivoRepository;
import com.unicity.inventory.repository.MovimientoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.unicity.inventory.repository.IntegrationTypeMappingRepository;

import java.util.List;
import java.util.Optional;

@Service
public class SyncService {

    private final ITGlueService itGlueService;
    private final ActivoRepository activoRepository;
    private final MovimientoRepository movimientoRepository;
    private final JumpCloudService jumpCloudService;


    private static final Long ITGLUE_STATUS_ACTIVE = 46600L;
    private static final Long ITGLUE_STATUS_INACTIVE = 46601L;

    public SyncService(ITGlueService itGlueService,
                       ActivoRepository activoRepository,
                       MovimientoRepository movimientoRepository,
                       JumpCloudService jumpCloudService) {
        this.itGlueService = itGlueService;
        this.activoRepository = activoRepository;
        this.movimientoRepository = movimientoRepository;
        this.jumpCloudService = jumpCloudService;
    }

    @Transactional
    public void syncActivo(Activo activo) {
        try {
            // Status
            Long statusId = esRetirado(activo) ? ITGLUE_STATUS_INACTIVE : ITGLUE_STATUS_ACTIVE;

            // Type ID según categoría
            String nombreCategoria = activo.getCategoria() != null
                    ? activo.getCategoria().getNombreCategoria() : null;
            Long typeId = itGlueService.getTypeIdForCategoria(nombreCategoria);

            // Ubicación actual desde el último movimiento
            String nombreUbicacion = getUbicacionActual(activo);
            Long locationId = itGlueService.getLocationIdForUbicacion(nombreUbicacion);

            // Fecha garantía
            String warrantyDate = activo.getFechaGarantia() != null
                    ? activo.getFechaGarantia().toString() : null;

            // Si ya tiene itglue_id → actualizar
            if (activo.getItglueId() != null) {
                itGlueService.updateConfiguration(
                        activo.getItglueId(),
                        activo.getEtiquetaInventario(),
                        activo.getNumeroDeSerie(),
                        activo.getMarca(),
                        activo.getModelo(),
                        typeId, statusId,
                        warrantyDate,
                        activo.getEtiquetaInventario(),
                        locationId
                );
                if (esRetirado(activo)) {
                    itGlueService.archiveConfiguration(activo.getItglueId());
                }
                syncJumpCloud(activo);
                return;
            }

            // Buscar por serial number
            if (activo.getNumeroDeSerie() != null && !activo.getNumeroDeSerie().isBlank()) {
                Optional<Long> existingId = itGlueService.findBySerialNumber(activo.getNumeroDeSerie());
                if (existingId.isPresent()) {
                    activo.setItglueId(existingId.get());
                    activoRepository.save(activo);
                    itGlueService.updateConfiguration(
                            existingId.get(),
                            activo.getEtiquetaInventario(),
                            activo.getNumeroDeSerie(),
                            activo.getMarca(),
                            activo.getModelo(),
                            typeId, statusId,
                            warrantyDate,
                            activo.getEtiquetaInventario(),
                            locationId
                    );
                    syncJumpCloud(activo);
                    return;
                }
            }

            // Crear nuevo
            Optional<Long> newId = itGlueService.createConfiguration(
                    activo.getEtiquetaInventario(),
                    activo.getNumeroDeSerie(),
                    activo.getMarca(),
                    activo.getModelo(),
                    typeId, statusId,
                    warrantyDate,
                    activo.getEtiquetaInventario(),
                    locationId
            );

            newId.ifPresent(id -> {
                activo.setItglueId(id);
                activoRepository.save(activo);
            });
            syncJumpCloud(activo);
        } catch (Exception e) {
            System.err.println("Error sincronizando activo "
                    + activo.getIdEquipo() + ": " + e.getMessage());
        }
    }

    // ✅ Obtiene la ubicación actual del activo desde su último movimiento
    private String getUbicacionActual(Activo activo) {
        List<Movimiento> movimientos = movimientoRepository
                .findByActivo_IdEquipoOrderByFechaMovimientoDesc(activo.getIdEquipo());
        if (!movimientos.isEmpty() && movimientos.get(0).getUbicacion() != null) {
            return movimientos.get(0).getUbicacion().getNombreUbicacion();
        }
        return "Bodega"; // fallback
    }

    private boolean esRetirado(Activo activo) {
        return activo.getEstado() != null &&
                activo.getEstado().getNombreEstado().equalsIgnoreCase("Retirado");
    }
    // ✅ Sincroniza con JumpCloud solo si es laptop/desktop/all in one
    private void syncJumpCloud(Activo activo) {
        String categoria = activo.getCategoria() != null
                ? activo.getCategoria().getNombreCategoria().toLowerCase()
                : "";

        boolean esDispositivo = categoria.contains("portatil")
                || categoria.contains("computador")
                || categoria.contains("all in one");

        if (!esDispositivo) return;

        try {
            // Si ya tiene jumpcloud_id → actualizar nombre
            if (activo.getJumpcloudId() != null) {
                jumpCloudService.updateSystem(
                        activo.getJumpcloudId(),
                        activo.getEtiquetaInventario()
                );
                return;
            }

            // Buscar por serial number
            if (activo.getNumeroDeSerie() != null
                    && !activo.getNumeroDeSerie().isBlank()) {
                Optional<String> existingId = jumpCloudService
                        .findBySerialNumber(activo.getNumeroDeSerie());
                if (existingId.isPresent()) {
                    activo.setJumpcloudId(existingId.get());
                    activoRepository.save(activo);
                    jumpCloudService.updateSystem(
                            existingId.get(),
                            activo.getEtiquetaInventario()
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Error sincronizando con JumpCloud activo "
                    + activo.getIdEquipo() + ": " + e.getMessage());
        }
    }

}