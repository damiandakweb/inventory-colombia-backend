package com.unicity.inventory.mapping;


import com.unicity.inventory.models.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapping {

    public UsuarioDto usuarioDto(Usuario usuario) {
        return new UsuarioDto(usuario.getIdUsuario(), usuario.getNombre(), usuario.getRol(), usuario.getEmail(), null);
    }

    public Usuario dtoToUsuario(UsuarioDto usuarioDto) {
        return new Usuario(
                usuarioDto.getIdUsuario(), // <-- Esta línea es clave
                usuarioDto.getNombre(),
                usuarioDto.getRol(),
                usuarioDto.getEmail(),
                usuarioDto.getPassword()
        );
    }
}