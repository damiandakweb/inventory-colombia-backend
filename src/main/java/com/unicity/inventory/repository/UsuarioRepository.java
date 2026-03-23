package com.unicity.inventory.repository;


import com.unicity.inventory.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Long> {
    Optional<Usuario> findByEmail(String email);
    @Query("SELECT u FROM Usuario u WHERE u.nombre = :nombre")
    Optional<Usuario> findByNombre(String nombre);
    boolean existsByEmail(String email);
}
