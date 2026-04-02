package com.unicity.inventory.repository;

import com.unicity.inventory.mapping.IntegrationTypeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationTypeMappingRepository extends JpaRepository<IntegrationTypeMapping, Long> {
    Optional<IntegrationTypeMapping> findByNombreCategoriaAndIntegration(
            String nombreCategoria, String integration);

    List<IntegrationTypeMapping> findAllByIntegration(String integration);
}