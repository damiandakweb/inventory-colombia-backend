package com.unicity.inventory.mapping;


import com.unicity.inventory.models.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapping {


    public CategoriaDto categoriaDto(Categoria categoria) {
        return new CategoriaDto(categoria.getIdCategoria(),categoria.getNombreCategoria(),categoria.getNombreEn());
    }
    public Categoria dtotoCategoria(CategoriaDto categoriaDto) {
        return new Categoria(categoriaDto.getIdCategoria(),categoriaDto.getNombreCategoria(),categoriaDto.getNombreEn());
    }
}
