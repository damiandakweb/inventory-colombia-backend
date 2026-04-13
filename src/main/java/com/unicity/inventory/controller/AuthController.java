package com.unicity.inventory.controller;

import com.unicity.inventory.config.Jwt.JwtUtils;
import com.unicity.inventory.mapping.LoginRequest;
import com.unicity.inventory.mapping.LoginResponse;
import com.unicity.inventory.mapping.UsuarioDto;
import com.unicity.inventory.mapping.UsuarioMapping;
import com.unicity.inventory.models.SecurityUser;
import com.unicity.inventory.models.Usuario;
import com.unicity.inventory.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UsuarioMapping usuarioMapping;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            String email = loginRequest.getEmail().toLowerCase().trim();

            // ✅ Validar formato obligatorio: admin.nombre@unicity.com O rol ALMACENISTA
            boolean emailValido = email.matches("^admin\\..+@unicity\\.com$");
            Optional<Usuario> usuarioBD = usuarioRepository.findByEmail(email);

            // ✅ Bloquear rol USUARIO — nunca puede hacer login
            if (usuarioBD.isPresent() &&
                    usuarioBD.get().getRol().equalsIgnoreCase("USUARIO")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error",
                                "Acceso denegado. Este usuario no tiene acceso a la aplicación."));
            }

            boolean tieneRolPermitido = usuarioBD.isPresent() &&
                    usuarioBD.get().getRol().equalsIgnoreCase("ALMACENISTA");

            if (!emailValido && !tieneRolPermitido) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error",
                                "Acceso denegado. Solo cuentas admin.nombre@unicity.com pueden ingresar."));
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(), loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
            Usuario usuario = userDetails.getUsuario();

            String jwt = jwtUtils.generateJwtToken(usuario.getEmail());
            UsuarioDto usuarioDto = usuarioMapping.usuarioDto(usuario);

            return ResponseEntity.ok(new LoginResponse(jwt, usuarioDto));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas"));
        }
    }
}