package com.unicity.inventory.Service;

import com.unicity.inventory.Mapping.DevolucionRequest;
import com.unicity.inventory.Mapping.ProcesarSolicitudRequest;
import com.unicity.inventory.Mapping.SolicitudDto;

import java.util.List;
import java.util.Optional;

public interface SolicitudService {

    boolean existByIdSolicitud(Long id);

    void crearNuevaSolicitud(SolicitudDto solicitudDto);

    boolean deleteSolicitud(Long id);

    List<SolicitudDto> getAllSolicitudes();

    Optional<SolicitudDto> findSolicitudById(Long id);

    void procesarSolicitud(Long solicitudId, ProcesarSolicitudRequest request);

    void procesarSolicitudDeCambio(Long solicitudId, ProcesarSolicitudRequest request);

    void enviarActivoAMantenimiento(Long solicitudId, Long activoId);

    void procesarSolicitudDeDevolucion(Long solicitudId, DevolucionRequest request);

    void rechazarSolicitud(Long solicitudId, String motivo);
}

