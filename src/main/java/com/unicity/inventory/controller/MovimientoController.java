package com.unicity.inventory.controller;


import com.unicity.inventory.Mapping.MovimientoDto;
import com.unicity.inventory.Mapping.MovimientoMapping;
import com.unicity.inventory.Models.Movimiento;
import com.unicity.inventory.Repository.MovimientoRepository;
import com.unicity.inventory.Service.MovimientoServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class MovimientoController {

    private final MovimientoServiceImpl movimientoServiceImpl;

    private final MovimientoMapping  movimientoMapping;

    private final MovimientoRepository movimientoRepository;


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<List<MovimientoDto>> getAllMovimientos() {
        List<Movimiento> movimientos = movimientoServiceImpl.getAllMovimientos();
        List<MovimientoDto> dtos = movimientos.stream()
                .map(movimientoMapping::movimientoToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<?> createMovimiento(@RequestBody MovimientoDto movimientoDto) {
        // Verificamos que todos los IDs necesarios estén presentes
        if (movimientoDto.getIdEquipo() == null || movimientoDto.getIdUsuario() == null || movimientoDto.getIdUbicacion() == null) {
            return ResponseEntity.badRequest().body("Error: Activo, Usuario y Ubicación son obligatorios.");
        }

        MovimientoDto movimientoCreado = movimientoServiceImpl.createMovimiento(movimientoDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(movimientoCreado);
    }

    @GetMapping("/activo/{activoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<List<MovimientoDto>> getMovimientosByActivoId(@PathVariable Long activoId) {
        // 1. Simplemente llamamos al servicio. Este ya devuelve List<MovimientoDto>.
        List<MovimientoDto> movimientosDto = movimientoServiceImpl.getMovimientosByActivoId(activoId);

        // 2. Devolvemos el resultado directamente.
        return ResponseEntity.ok(movimientosDto);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<Optional<MovimientoDto>>  updateMovimiento(@PathVariable Long id, @RequestBody MovimientoDto movimientoDto) {
        try {
            Optional <MovimientoDto> result = movimientoServiceImpl.updateMovimiento(id,movimientoDto);
            return ResponseEntity.ok(result);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovimiento(@PathVariable Long id) {
        boolean deleted = movimientoServiceImpl.deleteMovimiento(id);
        if (deleted) {
            return ResponseEntity.noContent().build(); // 204
        } else {
            return ResponseEntity.notFound().build(); // 404
        }
    }
}
