package com.unicity.inventory.mapping;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "integration_location_mapping")
public class IntegrationLocationMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_ubicacion")
    private String nombreUbicacion;

    @Column(name = "integration")
    private String integration;

    @Column(name = "external_location_name")
    private String externalLocationName;

    @Column(name = "external_location_id")
    private String externalLocationId;
}