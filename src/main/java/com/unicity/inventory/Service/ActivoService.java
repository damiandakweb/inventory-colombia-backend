package com.unicity.inventory.Service;


import com.unicity.inventory.Mapping.ActivoDto;
import com.unicity.inventory.Models.Activo;

import java.util.List;
import java.util.Optional;

public interface ActivoService {
    // Devuelve DTOs para el controlador
    List<ActivoDto> getAllActivos();
    Optional<ActivoDto> findActivoById(Long id);

    // Recibe DTOs para crear y actualizar
    ActivoDto createActivo(ActivoDto activoDto);
    Optional<ActivoDto> updateActivo(Long id, ActivoDto activoDto);

    // Maneja la baja lógica del activo
    void deleteActivo(Long id);

    List<ActivoDto> findActivoByUsuarioAndCategoria(Long usuarioId, Long categoriaId);
    List<ActivoDto> findDisponiblesByCategoria(Long categoriaId);

    List<ActivoDto> getActivosByUsuarioId(Long usuarioId);

    void enlazarActivo(Long activoId, Long relacionadoId);

    void desenlazarActivo(Long activoId, Long relacionadoId);

    List<ActivoDto> findActivosRelacionados(Long activoId);

}
