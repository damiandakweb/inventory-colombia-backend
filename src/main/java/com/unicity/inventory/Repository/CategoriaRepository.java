package com.unicity.inventory.Repository;


import com.unicity.inventory.Models.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface CategoriaRepository extends JpaRepository<Categoria,Long> {

    @Query("SELECT c FROM Categoria c WHERE c.nombreCategoria = :nombreCategoria")
    Optional<Categoria> findByNombreCategoria(String nombreCategoria);
}
