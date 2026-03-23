package com.unicity.inventory.models;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Table(name = "`Estado`")
@Getter
@Setter
public class Estado {

    @Id
    @Column(name = "ID_estado")
    private Long idEstado;

    @Column(name = "nombre_estado")
    private String nombreEstado;


    public Estado() {

    }

    public Estado(Long idEstado, String nombreEstado) {
        this.idEstado = idEstado;
        this.nombreEstado = nombreEstado;
    }
}
