package com.unicity.inventory.models;

import jakarta.persistence.*;
import lombok.*;


@Data
@Entity
@Table(name = "`Usuario`")
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_usuario")
    private Long idUsuario;

    private String nombre;
    private String rol;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = true)
    private String password = "";

    public Usuario(Long idUsuario, String nombre, String rol, String email, String password) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.rol = rol;
        this.email = email;
        this.password = password;
    }

    public Usuario() {

    }
    public Usuario(Long idUsuario, String nombre, String rol, String email) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.rol = rol;
        this.email = email;
        this.password = ""; // valor por defecto
    }
}
