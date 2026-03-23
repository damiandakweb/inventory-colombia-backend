package com.unicity.inventory.controller;

import com.unicity.inventory.mapping.CategoriaDto;
import com.unicity.inventory.mapping.CategoriaMapping;
import com.unicity.inventory.models.Categoria;
import com.unicity.inventory.service.CategoriaServicelmpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CategoriaController {

    private final CategoriaServicelmpl categoriaServicelmpl;
    private final CategoriaMapping categoriaMapping;

    @GetMapping
    public List<CategoriaDto> getAllCategorias() {
        System.out.println("--- PASO 3: Entrando a getAllCategorias en el Controlador ---");
        List<Categoria> categoriasDesdeServicio = categoriaServicelmpl.getAllCategoria();
        System.out.println("--- PASO 4: El Servicio devolvió " + categoriasDesdeServicio.size() + " categorías. ---");

        List<CategoriaDto> dtos = categoriasDesdeServicio.stream()
                .map(categoriaMapping::categoriaDto)
                .collect(Collectors.toList());

        System.out.println("--- PASO 5: Mapeo a DTO completo. Devolviendo respuesta. ---");
        return dtos;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<CategoriaDto> createCategoria(@RequestBody CategoriaDto categoriaDto) {
        Categoria categoriaGuardado = categoriaServicelmpl.saveCategoria(categoriaDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaMapping.categoriaDto(categoriaGuardado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDto> findCategoriaById(@PathVariable Long id){
        return categoriaServicelmpl.findCategoriaById(id)
                .map(categoria -> ResponseEntity.ok(categoriaMapping.categoriaDto(categoria)))
                .orElse(ResponseEntity.notFound().build());
        }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<CategoriaDto> updateCategoria(@PathVariable Long id, @RequestBody CategoriaDto categoriaDto) {
        return categoriaServicelmpl.updateCategoria(id, categoriaDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<Void> deleteCategoria(@PathVariable Long id) {
        if (categoriaServicelmpl.deleteCategoria(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

