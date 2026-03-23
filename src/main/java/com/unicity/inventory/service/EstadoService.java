package com.unicity.inventory.service;

import com.unicity.inventory.mapping.EstadoDto;
import com.unicity.inventory.models.Estado;

import java.util.List;
import java.util.Optional;

public interface EstadoService {


    boolean existByEstado(Long id);

    Estado saveEstado(EstadoDto estadoDto);

    boolean deleteEstado(Long id);

    List<Estado> getAllEstado();

    Optional<Estado> findEstadoById(Long id);

    Optional<EstadoDto> updateEstado(Long id, EstadoDto estadoDetails);

}
