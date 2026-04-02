package com.unicity.inventory.repository;

import com.unicity.inventory.mapping.IntegrationLocationMapping;
import com.unicity.inventory.mapping.IntegrationTypeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationLocationMappingRepository extends JpaRepository<IntegrationLocationMapping, Long> {
    Optional<IntegrationLocationMapping> findByNombreUbicacionAndIntegration(
            String nombreUbicacion, String integration);
}