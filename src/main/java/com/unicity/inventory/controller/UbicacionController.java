package com.unicity.inventory.controller;

import com.unicity.inventory.mapping.UbicacionDto;
import com.unicity.inventory.mapping.UbicacionMapping;
import com.unicity.inventory.models.Ubicacion;
import com.unicity.inventory.service.UbicacionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ubicaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class UbicacionController {

    @Autowired
    private UbicacionMapping ubicacionMapping;

    @Autowired
    private UbicacionServiceImpl ubicacionServiceImpl;

    @GetMapping
    public ResponseEntity<List<UbicacionDto>> getAllUbicacion() {
        List<Ubicacion> ubicacionResult = ubicacionServiceImpl.getAllUbicacion();
        return ResponseEntity.ok(ubicacionResult.stream()
                .map(ubicacion -> ubicacionMapping.ubicacionDto(ubicacion))
                .collect(Collectors.toList())
        );
    }
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<UbicacionDto> createUbicacion(@RequestBody UbicacionDto ubicacionDto) {
        // 1. El controlador recibe el DTO
        // 2. Llama al servicio, que también espera un DTO
        Ubicacion ubicacionGuardada = ubicacionServiceImpl.saveUbicacion(ubicacionDto);

        // 3. Convierte la entidad guardada de nuevo a DTO para la respuesta
        return ResponseEntity.status(HttpStatus.CREATED).body(ubicacionMapping.ubicacionDto(ubicacionGuardada));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UbicacionDto> findUbicacionById(@PathVariable Long id){
        return ubicacionServiceImpl.findUbicacionById(id)
                .map(ubicacion -> ResponseEntity.ok(ubicacionMapping.ubicacionDto(ubicacion)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<UbicacionDto> updateUbicacion(@PathVariable Long id, @RequestBody UbicacionDto ubicacionDto) {
        return ubicacionServiceImpl.updateUbicacion(id, ubicacionDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<Void> deleteUbicacion(@PathVariable Long id) {
        if (ubicacionServiceImpl.deleteUbicacion(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
