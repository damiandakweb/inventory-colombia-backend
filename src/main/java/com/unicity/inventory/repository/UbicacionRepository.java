package com.unicity.inventory.repository;

import com.unicity.inventory.models.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface UbicacionRepository extends JpaRepository<Ubicacion,Long> {
    Optional<Ubicacion> findByNombreUbicacion(String nombreUbicacion);
}
