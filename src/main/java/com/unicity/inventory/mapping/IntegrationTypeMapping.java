package com.unicity.inventory.mapping;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "integration_type_mapping")
public class IntegrationTypeMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_categoria")
    private String nombreCategoria;

    @Column(name = "integration")
    private String integration;

    @Column(name = "external_type_name")
    private String externalTypeName;

    @Column(name = "external_type_id")
    private String externalTypeId;
}