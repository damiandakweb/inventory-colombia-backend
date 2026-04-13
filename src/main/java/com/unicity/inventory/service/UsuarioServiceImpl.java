package com.unicity.inventory.service;

import com.unicity.inventory.mapping.UsuarioDto;
import com.unicity.inventory.mapping.UsuarioMapping;
import com.unicity.inventory.models.Usuario;
import com.unicity.inventory.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapping usuarioMapping;
    private final PasswordEncoder passwordEncoder;


    @Override
    public boolean existsById(Long id) {
        // ✅ CORREGIDO: Se pasa el 'id' directamente, sin convertirlo.
        return usuarioRepository.existsById(id);
    }

    @Override
    public UsuarioDto createUser(UsuarioDto usuarioDto) {
        if (usuarioRepository.existsByEmail(usuarioDto.getEmail())) {
            throw new IllegalStateException("El correo electrónico '"
                    + usuarioDto.getEmail() + "' ya está registrado.");
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(usuarioDto.getNombre());
        nuevoUsuario.setEmail(usuarioDto.getEmail());

        // ✅ Rol viene del DTO, por defecto USUARIO
        String rol = (usuarioDto.getRol() != null && !usuarioDto.getRol().isBlank())
                ? usuarioDto.getRol().toUpperCase()
                : "USUARIO";
        nuevoUsuario.setRol(rol);

        // ✅ Contraseña solo para ALMACENISTA
        if (rol.equals("ALMACENISTA") && usuarioDto.getPassword() != null
                && !usuarioDto.getPassword().isBlank()) {
            nuevoUsuario.setPassword(passwordEncoder.encode(usuarioDto.getPassword()));
        } else {
            nuevoUsuario.setPassword("");
        }

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
        return usuarioMapping.usuarioDto(usuarioGuardado);
    }

    @Override
    public void deleteUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id); // Lanza error si no existe
        }
        usuarioRepository.deleteById(id);
    }

    @Override
    public List<UsuarioDto> getAllUsuarios() {
        // ✅ LÓGICA DE MAPEADO MOVIDA AQUÍ
        return usuarioRepository.findAll().stream()
                .map(usuarioMapping::usuarioDto) // Asegúrate que el método se llame así
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UsuarioDto> findUsuarioById(Long id) {
        return usuarioRepository.findById(id)
                .map(usuarioMapping::usuarioDto); // Mapea directamente a DTO
    }

    @Override
    public Optional<UsuarioDto> updateUsuario(Long id, UsuarioDto usuarioDetails) {
        return usuarioRepository.findById(id)
                .map(usuarioExistente -> {
                    usuarioExistente.setNombre(usuarioDetails.getNombre());
                    usuarioExistente.setEmail(usuarioDetails.getEmail());
                    Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);
                    return usuarioMapping.usuarioDto(usuarioActualizado);
                });
    }
}