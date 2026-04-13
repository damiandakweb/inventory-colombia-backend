package com.unicity.inventory.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.unicity.inventory.config.Jwt.JwtUtils;
import com.unicity.inventory.mapping.LoginResponse;
import com.unicity.inventory.mapping.UsuarioDto;
import com.unicity.inventory.mapping.UsuarioMapping;
import com.unicity.inventory.models.Usuario;
import com.unicity.inventory.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class GoogleAuthController {

    @Value("${GOOGLE_CLIENT_ID}")
    private String googleClientId;

    private final UsuarioRepository usuarioRepository;
    private final JwtUtils jwtUtils;
    private final UsuarioMapping usuarioMapping;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
        String idTokenStr = body.get("token");

        if (idTokenStr == null || idTokenStr.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Token de Google no proporcionado."));
        }

        try {
            // 1. Verificar el token con Google
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenStr);
            if (idToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token de Google inválido."));
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail().toLowerCase().trim();
            String nombre = (String) payload.get("name");

            // 2. Verificar acceso — solo admin. o rol ALMACENISTA
            boolean esAdmin = email.startsWith("admin.");
            Optional<Usuario> usuarioBD = usuarioRepository.findByEmail(email);
            boolean tieneRolPermitido = usuarioBD.isPresent() &&
                    usuarioBD.get().getRol().equalsIgnoreCase("ALMACENISTA");

            if (!esAdmin && !tieneRolPermitido) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error",
                                "Acceso denegado. Solo cuentas autorizadas pueden ingresar."));
            }

            // 3. Buscar o crear el usuario
            Usuario usuario = usuarioBD.orElseGet(() -> {
                Usuario nuevo = new Usuario();
                nuevo.setEmail(email);
                nuevo.setNombre(nombre != null ? nombre : email);
                nuevo.setRol("ADMIN");
                nuevo.setPassword(passwordEncoder.encode(
                        java.util.UUID.randomUUID().toString()));
                return usuarioRepository.save(nuevo);
            });

            // 4. Generar JWT propio
            String jwt = jwtUtils.generateJwtToken(usuario.getEmail());
            UsuarioDto usuarioDto = usuarioMapping.usuarioDto(usuario);

            return ResponseEntity.ok(new LoginResponse(jwt, usuarioDto));

        } catch (Exception e) {
            System.err.println("Error en Google login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al verificar el token de Google."));
        }
    }
}