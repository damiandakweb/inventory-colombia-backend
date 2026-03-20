package com.unicity.inventory.Mapping;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Incluye Getters, Setters, etc.
@AllArgsConstructor // <-- Crea el constructor con (String token, UsuarioDto usuario)
@NoArgsConstructor  // <-- Crea un constructor vacío (buena práctica)
public class LoginResponse {
    // Dejamos solo los campos que el frontend realmente necesita en una respuesta exitosa
    private String token;
    private UsuarioDto usuario;
}