package com.unicity.inventory.Mapping;


import com.unicity.inventory.Models.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapping {


    public CategoriaDto categoriaDto(Categoria categoria) {
        return new CategoriaDto(categoria.getIdCategoria(),categoria.getNombreCategoria());
    }
    public Categoria dtotoCategoria(CategoriaDto categoriaDto) {
        return new Categoria(categoriaDto.getIdCategoria(),categoriaDto.getNombreCategoria());
    }
}
