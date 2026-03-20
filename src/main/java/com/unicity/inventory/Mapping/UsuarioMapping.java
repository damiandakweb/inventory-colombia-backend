package com.unicity.inventory.Mapping;


import com.unicity.inventory.Models.Usuario;
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