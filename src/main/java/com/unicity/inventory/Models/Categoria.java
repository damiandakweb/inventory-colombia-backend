package com.unicity.inventory.Models;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Table(name = "`Categoria`")
@Getter
@Setter
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_categoria")
    private Long idCategoria;

    @Column(name = "nombre_categoria") // Mapea al nombre de la columna en la BD
    private String nombreCategoria;

    public Categoria() {

    }

    public Categoria(Long idCategoria, String nombreCategoria) {
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
    }
}
