package com.unicity.inventory.Service;


import com.unicity.inventory.Mapping.CategoriaDto;
import com.unicity.inventory.Mapping.CategoriaMapping;
import com.unicity.inventory.Models.Categoria;
import com.unicity.inventory.Repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaServicelmpl implements CategoriaService {


    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapping categoriaMapping;

    @Autowired
    public CategoriaServicelmpl(CategoriaRepository categoriaRepository, CategoriaMapping categoriaMapping) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapping = categoriaMapping;
    }

    @Override
    public boolean existByIdCategoria(Long id) {

        return categoriaRepository.existsById(id);
    }

    @Override
    public Categoria saveCategoria(CategoriaDto categoriaDto) {

        Categoria categoria = categoriaMapping.dtotoCategoria(categoriaDto);

        return categoriaRepository.save(categoria);
    }

    @Override
    public boolean deleteCategoria(Long id) {
        if (categoriaRepository.existsById(id)) {
            categoriaRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<Categoria> getAllCategoria () {
        System.out.println("--- PASO 1: Entrando a getAllCategorias en el Servicio ---");
        List<Categoria> resultado = categoriaRepository.findAll();
        System.out.println("--- PASO 2: El Repositorio encontró " + resultado.size() + " categorías. ---");
        return resultado;
    }

    @Override
    public Optional<Categoria> findCategoriaById(Long id) {
        return categoriaRepository.findById(id);
    }

    @Override
    public Optional<CategoriaDto> updateCategoria(Long id, CategoriaDto categoriaDto) {
        // Buscamos la categoría existente por su ID
        return categoriaRepository.findById(id)
                .map(categoriaExistente -> {
                    // Si existe, actualizamos su nombre con el del DTO
                    categoriaExistente.setNombreCategoria(categoriaDto.getNombreCategoria());
                    // Guardamos la entidad actualizada
                    Categoria categoriaActualizada = categoriaRepository.save(categoriaExistente);
                    // Devolvemos el DTO del resultado
                    return Optional.of(categoriaMapping.categoriaDto(categoriaActualizada));
                })
                .orElse(Optional.empty()); // Si no se encuentra, devuelve un Optional vacío
    }
}
