package com.unicity.inventory.controller;

import com.unicity.inventory.mapping.EstadoDto;
import com.unicity.inventory.mapping.EstadoMapping;
import com.unicity.inventory.models.Estado;
import com.unicity.inventory.service.EstadoServicelmpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/estados")
@RequiredArgsConstructor // ✅ Usamos solo Lombok para la inyección
@CrossOrigin(origins = "http://localhost:5173")
public class EstadoController {

    private final EstadoServicelmpl estadoService; // ✅ Inyectamos la interfaz del servicio
    private final EstadoMapping estadoMapping;

    @GetMapping
    public List<EstadoDto> getAllEstado() { // ✅ No necesita ResponseEntity si todo va bien
        return estadoService.getAllEstado().stream()
                .map(estadoMapping::estadoDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<EstadoDto> createEstado(@RequestBody EstadoDto estadoDto) {
        Estado estadoGuardado = estadoService.saveEstado(estadoDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(estadoMapping.estadoDto(estadoGuardado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstadoDto> findEstadoById(@PathVariable Long id){ // Asumiendo que el ID es Long
        return estadoService.findEstadoById(id)
                .map(estado -> ResponseEntity.ok(estadoMapping.estadoDto(estado)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}") // ✅ CORREGIDO
    public ResponseEntity<EstadoDto> updateEstado(@PathVariable Long id, @RequestBody EstadoDto estadoDto) {
        return estadoService.updateEstado(id, estadoDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEstado(@PathVariable Long id) {
        if (estadoService.deleteEstado(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}