package com.unicity.inventory.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "`Activo`")
public class Activo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_equipo")
    private Long idEquipo;

    @Column(name = "numero_de_serie")
    private String numeroDeSerie;

    @Column(name = "fecha_garantia")
    private LocalDate fechaGarantia;
    @Column(name = "fecha_compra")
    private LocalDate fechaCompra;

    @ManyToOne
    @JoinColumn(name = "id_categoria", referencedColumnName = "ID_categoria")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "id_estado", referencedColumnName = "ID_estado")
    private Estado estado;

    @ManyToOne
    @JoinColumn(name = "id_usuario_actual")
    private Usuario usuarioActual;

    @Column(name = "etiqueta_inventario")
    private String etiquetaInventario;

    @Column(name = "marca")
    private String marca;

    @Column(name = "modelo")
    private String modelo;

    @Column(name = "pais", length = 2)
    private String pais;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "activo_relacionado",
            joinColumns = @JoinColumn(name = "activo_id", referencedColumnName = "ID_equipo"),
            inverseJoinColumns = @JoinColumn(name = "relacionado_id", referencedColumnName = "ID_equipo")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Set<Activo> activosRelacionados = new HashSet<>();

}
