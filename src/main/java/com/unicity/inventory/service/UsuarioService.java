package com.unicity.inventory.service;

import com.unicity.inventory.mapping.UsuarioDto;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {

    boolean existsById(Long id);


    UsuarioDto createUser(UsuarioDto usuarioDto);

    void deleteUsuario(Long id);

    List<UsuarioDto> getAllUsuarios();

    Optional<UsuarioDto> findUsuarioById(Long id);

    Optional<UsuarioDto> updateUsuario(Long id, UsuarioDto usuarioDetails);

}
