package com.unicity.inventory.controller;


import com.unicity.inventory.mapping.*;
import com.unicity.inventory.repository.SolicitudRepository;
import com.unicity.inventory.service.SolicitudServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class SolicitudController {

    @Autowired
    private SolicitudServiceImpl solicitudServiceImpl;

    @Autowired
    private SolicitudMapping   solicitudMapping;

    @Autowired
    private SolicitudRepository solicitudRepository;


    @GetMapping("/pendientes/count")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<Long> countSolicitudesPendientes() {
        // Usamos el método que ya existe en el repositorio
        long count = solicitudRepository.countByEstadoSolicitud("Nuevo");
        return ResponseEntity.ok(count);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public List<SolicitudDto> getAllSolicitudes() {
        return solicitudServiceImpl.getAllSolicitudes();
    }

    @PostMapping
    public ResponseEntity<String> receiveSolicitudFromScript(@RequestBody SolicitudDto solicitudDto) {
        solicitudServiceImpl.crearNuevaSolicitud(solicitudDto);
        return ResponseEntity.ok("Solicitud recibida y procesada");
    }

    @PostMapping("/{id}/procesar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<Void> procesarSolicitud(@PathVariable Long id, @RequestBody ProcesarSolicitudRequest request) {
        solicitudServiceImpl.procesarSolicitud(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudDto> findSolicitudById(@PathVariable Long id) {
        return solicitudServiceImpl.findSolicitudById(id)
                .map(dto -> ResponseEntity.ok(dto)) // Simplemente envuelve el DTO que ya viene del servicio
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSolicitud(@PathVariable Long id) {
        boolean deleted = solicitudServiceImpl.deleteSolicitud(id);
        if (deleted) {
            return ResponseEntity.noContent().build(); // 204
        } else {
            return ResponseEntity.notFound().build(); // 404
        }
    }

    @PostMapping("/{id}/procesar-cambio")
    public ResponseEntity<Void> procesarSolicitudDeCambio(@PathVariable Long id, @RequestBody ProcesarSolicitudRequest request) {
        solicitudServiceImpl.procesarSolicitudDeCambio(id, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/mantenimiento")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<Void> enviarAMantenimiento(
            @PathVariable("id") Long solicitudId,     // El ID de la solicitud desde la ruta
            @RequestParam("activoId") Long activoId   // El ID del activo desde el parámetro de la URL
    ) {
        // Llama al servicio con ambos IDs
        solicitudServiceImpl.enviarActivoAMantenimiento(solicitudId, activoId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/devolucion")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<Void> procesarDevolucion(
            @PathVariable Long id,
            @RequestBody DevolucionRequest request
    ) {
        solicitudServiceImpl.procesarSolicitudDeDevolucion(id, request);
        return ResponseEntity.ok().build();
    }
}
