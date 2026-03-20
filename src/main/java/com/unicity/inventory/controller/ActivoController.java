package com.unicity.inventory.controller;

import com.unicity.inventory.Mapping.ActivoDto;
import com.unicity.inventory.Service.ActivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping; // <-- Añadir import
import org.springframework.web.bind.annotation.PathVariable; // <-- Añadir import
import java.util.List; // <-- Añadir import

@RestController
@RequestMapping("/api/activos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ActivoController {

    private final ActivoService activoService;

    @GetMapping
    public List<ActivoDto> getAllActivos() {
        return activoService.getAllActivos();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<ActivoDto> getActivoById(@PathVariable Long id) {
        return activoService.findActivoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Ahora aceptamos el ID del activo a relacionar como parte de la URL
    @PostMapping("/{id}/enlazar/{relacionadoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<Void> enlazarActivo(@PathVariable Long id, @PathVariable Long relacionadoId) {
        activoService.enlazarActivo(id, relacionadoId);
        return ResponseEntity.ok().build();
    }

    // --- ✅ ENDPOINT ACTUALIZADO ---
    @PostMapping("/{id}/desenlazar/{relacionadoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<Void> desenlazarActivo(@PathVariable Long id, @PathVariable Long relacionadoId) {
        activoService.desenlazarActivo(id, relacionadoId);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<?> createActivo(@RequestBody ActivoDto activoDto) {
        // Verificamos que los IDs de las relaciones no sean nulos
        if (activoDto.getIdCategoria() == null || activoDto.getIdEstado() == null) {
            return ResponseEntity.badRequest().body("Error: Categoría y Estado son obligatorios.");
        }
        ActivoDto nuevoActivo = activoService.createActivo(activoDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoActivo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<?> updateActivo(@PathVariable Long id, @RequestBody ActivoDto activoDto) {
        // Verificamos que los IDs de las relaciones no sean nulos
        if (activoDto.getIdCategoria() == null || activoDto.getIdEstado() == null) {
            return ResponseEntity.badRequest().body("Error: Categoría y Estado son obligatorios.");
        }

        return activoService.updateActivo(id, activoDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<Void> deleteActivo(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Sin motivo especificado") String motivo) {
        activoService.deleteActivo(id, motivo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/disponibles")
    public List<ActivoDto> getActivosDisponiblesPorCategoria(@RequestParam Long categoriaId) {
        return activoService.findDisponiblesByCategoria(categoriaId);
    }

    @GetMapping("/usuario/{usuarioId}/categoria/{categoriaId}")
    public List<ActivoDto> getActivosPorUsuarioYCategoria(
            @PathVariable Long usuarioId, @PathVariable Long categoriaId) {
        // La lógica para llamar al servicio y mapear la lista a DTOs
        return activoService.findActivoByUsuarioAndCategoria(usuarioId, categoriaId);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<ActivoDto> getActivosPorUsuario(@PathVariable Long usuarioId) {
        return activoService.getActivosByUsuarioId(usuarioId);
    }

    @GetMapping("/{id}/relacionados")
    // Puedes ajustar los permisos si es necesario
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<List<ActivoDto>> getActivosRelacionados(@PathVariable Long id) {
        try {
            List<ActivoDto> relacionados = activoService.findActivosRelacionados(id);
            return ResponseEntity.ok(relacionados);
        } catch (RuntimeException e) {
            // Manejo básico de error si el activo principal no se encuentra
            return ResponseEntity.notFound().build();
        }
    }
}