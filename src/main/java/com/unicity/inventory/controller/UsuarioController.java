package com.unicity.inventory.controller;

import com.unicity.inventory.Mapping.UsuarioDto;
import com.unicity.inventory.Mapping.UsuarioMapping;
import com.unicity.inventory.Models.Usuario;
import com.unicity.inventory.Service.UsuarioServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioServiceImpl usuarioService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<List<UsuarioDto>> getAllUsuarios() {
        // El controlador solo llama al servicio y devuelve la respuesta
        return ResponseEntity.ok(usuarioService.getAllUsuarios());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<?> createUser(@RequestBody UsuarioDto usuarioDto) {
        try {
            // El controlador solo delega la creación al servicio
            UsuarioDto usuarioCreado = usuarioService.createUser(usuarioDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCreado);
        } catch (IllegalStateException e) {
            // Si el servicio lanza un error (ej. email duplicado), el controlador lo atrapa
            // y devuelve una respuesta HTTP apropiada (409 Conflict).
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<UsuarioDto> findUsuarioById(@PathVariable Long id) {
        return usuarioService.findUsuarioById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    public ResponseEntity<UsuarioDto> updateUsuario(@PathVariable Long id, @RequestBody UsuarioDto usuarioDto) {
        return usuarioService.updateUsuario(id, usuarioDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Long id) {
        try {
            usuarioService.deleteUsuario(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (Exception e) {
            return ResponseEntity.notFound().build(); // 404 Not Found si el ID no existe
        }
    }
}
