package com.unicity.inventory.models;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "`Movimiento`")
@Setter
@Getter
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_movimiento")
    private Long idMovimiento;

    @Column(name = "tipo_de_movimiento")
    private String tipoDeMovimiento;

    @Column(name = "fecha_movimiento")
    private LocalDate fechaMovimiento;

    @Column(name = "observacion")
    private String observacion;


    // Relación con Activo
    @ManyToOne
    @JoinColumn(name = "id_equipo", referencedColumnName = "ID_equipo")
    private Activo activo;

    // Relación con Usuario
    @ManyToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "ID_usuario")
    private Usuario usuario;

    // Relación con Ubicacion
    @ManyToOne
    @JoinColumn(name = "id_ubicacion", referencedColumnName = "ID_ubicacion")
    private Ubicacion ubicacion;

}
