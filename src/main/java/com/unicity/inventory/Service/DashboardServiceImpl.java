package com.unicity.inventory.Service;

import com.unicity.inventory.Mapping.DashboardSummaryDto;
import com.unicity.inventory.Mapping.MovimientoMapping;
import com.unicity.inventory.Models.Movimiento;
import com.unicity.inventory.Repository.ActivoRepository;
import com.unicity.inventory.Repository.MovimientoRepository;
import com.unicity.inventory.Repository.SolicitudRepository;
import com.unicity.inventory.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable; // <-- EL IMPORT CORRECTO
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ActivoRepository activoRepository;
    private final UsuarioRepository usuarioRepository;
    private final SolicitudRepository solicitudRepository;
    private final MovimientoRepository movimientoRepository;
    private final MovimientoMapping movimientoMapping;

    @Override
    public DashboardSummaryDto getDashboardSummary() {
        DashboardSummaryDto summary = new DashboardSummaryDto();

        // --- Lógica para estadísticas generales ---
        summary.setTotalActivos(activoRepository.count());
        summary.setTotalUsuarios(usuarioRepository.count());
        summary.setSolicitudesPendientes(solicitudRepository.countByEstadoSolicitud("Nuevo"));
        summary.setActivosPorEstado(activoRepository.countActivosByEstado());

        // --- LÓGICA CORREGIDA PARA COMPUTADORES DE BACKUP ---

        // 1. Define la lista de estados que consideramos "disponibles".
        // Asumiendo que 1L = 'Nuevo'/'Disponible' y 6L = 'En Bodega'.
        List<Long> estadosDisponiblesIds = Arrays.asList(1L, 6L);

        // 2. Llama al nuevo método del repositorio para contar los computadores/portátiles en esos estados.
        // Usamos "Portatil" como en tu código original. Cambia a "Computador" si es necesario.
        long computadoresBackup = activoRepository.countBackupByCategoriaAndEstado(
                "Portatil",
                estadosDisponiblesIds
        );

        summary.setComputadoresBackup(computadoresBackup);

        // --- Lógica para obtener los últimos movimientos ---
        // Asumiendo que tienes un método findTop5 en tu MovimientoRepository
        List<Movimiento> ultimosMovimientos = movimientoRepository.findTop5();
        summary.setUltimosMovimientos(ultimosMovimientos.stream()
                .map(movimientoMapping::movimientoToDto)
                .collect(Collectors.toList()));

        return summary;
    }
}