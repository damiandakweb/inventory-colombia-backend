package com.unicity.inventory.mapping;

public class UsuarioDto {


    private Long idUsuario;
    private String nombre;
    private String rol;
    private String email;
    private String password;

    public UsuarioDto(Long idUsuario, String nombre, String rol, String email, String password) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.rol = rol;
        this.email = email;
        this.password = password;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UsuarioDto() {

    }

}
