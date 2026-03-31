package com.unicity.inventory.mapping;

public class CategoriaDto {

    private Long idCategoria;
    private String nombreCategoria;
    private String nombreEn;

    public CategoriaDto(Long idCategoria, String nombreCategoria, String nombreEn) {
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
        this.nombreEn = nombreEn;
    }

     public CategoriaDto() {

     }

    public Long getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Long idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public String getNombreEn() { return nombreEn; }

    public void setNombreEn(String nombreEn) { this.nombreEn = nombreEn; }
}
