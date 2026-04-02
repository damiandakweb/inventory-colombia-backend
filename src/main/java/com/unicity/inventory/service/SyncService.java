package com.unicity.inventory.service;

import com.unicity.inventory.models.Activo;
import com.unicity.inventory.models.Movimiento;
import com.unicity.inventory.repository.ActivoRepository;
import com.unicity.inventory.repository.MovimientoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SyncService {

    private final ITGlueService itGlueService;
    private final ActivoRepository activoRepository;
    private final MovimientoRepository movimientoRepository;

    private static final Long ITGLUE_STATUS_ACTIVE = 46600L;
    private static final Long ITGLUE_STATUS_INACTIVE = 46601L;

    public SyncService(ITGlueService itGlueService,
                       ActivoRepository activoRepository,
                       MovimientoRepository movimientoRepository) {
        this.itGlueService = itGlueService;
        this.activoRepository = activoRepository;
        this.movimientoRepository = movimientoRepository;
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
}