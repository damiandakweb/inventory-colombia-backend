package com.unicity.inventory.Service;

import com.unicity.inventory.Mapping.UbicacionDto;
import com.unicity.inventory.Models.Ubicacion;

import java.util.List;
import java.util.Optional;

public interface UbicacionService {

    boolean existByUbicacion(Long id);

    Ubicacion saveUbicacion(UbicacionDto ubicacionDto);

    boolean deleteUbicacion(Long id);

    List<Ubicacion> getAllUbicacion();

    Optional<Ubicacion> findUbicacionById(Long id);

    Optional<UbicacionDto> updateUbicacion(Long id, UbicacionDto ubicacionDetails);
}
