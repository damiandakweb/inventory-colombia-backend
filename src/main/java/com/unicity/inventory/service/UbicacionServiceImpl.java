package com.unicity.inventory.service;


import com.unicity.inventory.mapping.UbicacionDto;
import com.unicity.inventory.mapping.UbicacionMapping;
import com.unicity.inventory.models.Ubicacion;
import com.unicity.inventory.repository.UbicacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UbicacionServiceImpl implements UbicacionService{

    private final UbicacionRepository ubicacionRepository;
    private final UbicacionMapping ubicacionMapping;

    @Autowired

    public UbicacionServiceImpl(UbicacionRepository ubicacionRepository, UbicacionMapping ubicacionMapping) {
        this.ubicacionRepository = ubicacionRepository;
        this.ubicacionMapping = ubicacionMapping;
    }

    @Override
    public boolean existByUbicacion(Long id) {
        return ubicacionRepository.existsById(id);
    }

    @Override
    public Ubicacion saveUbicacion(UbicacionDto ubicacionDto) {
        // Convierte el DTO a entidad antes de guardar
        Ubicacion ubicacion = ubicacionMapping.dtotoUbicacion(ubicacionDto);
        return ubicacionRepository.save(ubicacion);
    }

    @Override
    public boolean deleteUbicacion(Long id) {
            if (ubicacionRepository.existsById(id)) {
                ubicacionRepository.deleteById(id);
                return true;
            } else {
                return false;
            }
    }

    @Override
    public List<Ubicacion> getAllUbicacion(){
        return ubicacionRepository.findAll();
    }

    @Override
    public Optional<Ubicacion> findUbicacionById(Long id) {
        return  ubicacionRepository.findById(id);
    }

    @Override
    public Optional<UbicacionDto> updateUbicacion(Long id, UbicacionDto ubicacionDetails) {
        return ubicacionRepository.findById(id)
                .map(ubicacionExistente -> {
                    // Actualizamos el campo que viene del DTO
                    ubicacionExistente.setNombreUbicacion(ubicacionDetails.getNombreUbicacion());
                    ubicacionExistente.setNombreEn(ubicacionDetails.getNombreEn());
                    // Guardamos la entidad modificada
                    Ubicacion actualizada = ubicacionRepository.save(ubicacionExistente);
                    // Devolvemos el DTO actualizado
                    return Optional.of(ubicacionMapping.ubicacionDto(actualizada));
                })
                .orElse(Optional.empty()); // Si no se encuentra, devuelve un Optional vacío
    }
}
