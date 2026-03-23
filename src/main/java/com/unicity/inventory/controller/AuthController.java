package com.unicity.inventory.controller;

import com.unicity.inventory.config.Jwt.JwtUtils;
import com.unicity.inventory.mapping.LoginRequest;
import com.unicity.inventory.mapping.LoginResponse;
import com.unicity.inventory.mapping.UsuarioDto;
import com.unicity.inventory.mapping.UsuarioMapping;
import com.unicity.inventory.models.SecurityUser;
import com.unicity.inventory.models.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UsuarioMapping usuarioMapping;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // ✅ CORRECCIÓN: El cast ahora funciona y es más eficiente
            SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
            Usuario usuario = userDetails.getUsuario(); // Obtenemos la entidad Usuario directamente

            String jwt = jwtUtils.generateJwtToken(usuario.getEmail());
            UsuarioDto usuarioDto = usuarioMapping.usuarioDto(usuario);

            return ResponseEntity.ok(new LoginResponse(jwt, usuarioDto));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("error", "Credenciales inválidas"));
        }
    }
}