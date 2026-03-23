package com.unicity.inventory.service;

import com.unicity.inventory.mapping.MovimientoDto;
import com.unicity.inventory.models.Movimiento;

import java.util.List;
import java.util.Optional;

public interface MovimientoService {


    boolean existByIdMovimiento(Long id);

    MovimientoDto createMovimiento (MovimientoDto movimientoDto);

    boolean deleteMovimiento(Long id);

    List<Movimiento> getAllMovimientos();

    List<MovimientoDto> getMovimientosByActivoId(Long activoId);

    Optional<Movimiento> findMovimientoById(Long id);

    Optional<MovimientoDto> updateMovimiento(Long id, MovimientoDto movimientoDetails);
}
