package com.unicity.inventory.Service;

import com.unicity.inventory.Mapping.EstadoDto;
import com.unicity.inventory.Models.Estado;

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
