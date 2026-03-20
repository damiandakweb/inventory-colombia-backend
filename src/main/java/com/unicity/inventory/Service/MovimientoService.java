package com.unicity.inventory.Service;

import com.unicity.inventory.Mapping.MovimientoDto;
import com.unicity.inventory.Models.Movimiento;

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
