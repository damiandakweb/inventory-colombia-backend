package com.unicity.inventory.Service;

import com.unicity.inventory.Mapping.CategoriaDto;
import com.unicity.inventory.Models.Categoria;

import java.util.List;
import java.util.Optional;

public interface CategoriaService {


    boolean existByIdCategoria(Long id);

    Categoria saveCategoria(CategoriaDto categoriaDto);

    boolean deleteCategoria(Long id);

    List<Categoria> getAllCategoria();

    Optional<Categoria> findCategoriaById(Long id);

    Optional<CategoriaDto> updateCategoria(Long id,  CategoriaDto categoriaDetails);
}
