package com.unicity.inventory.service;

import com.unicity.inventory.mapping.EstadoDto;
import com.unicity.inventory.mapping.EstadoMapping;
import com.unicity.inventory.models.Estado;
import com.unicity.inventory.repository.EstadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EstadoServicelmpl implements EstadoService {

    private final EstadoRepository estadoRepository;
    private final EstadoMapping estadoMapping;

    @Autowired
    public EstadoServicelmpl(EstadoRepository estadoRepository, EstadoMapping estadoMapping) {
        this.estadoRepository = estadoRepository;
        this.estadoMapping = estadoMapping;
    }

    @Override
    public boolean existByEstado(Long id) {
        return estadoRepository.existsById(id);
    }

    @Override
    public Estado saveEstado(EstadoDto estadoDto) {
        Estado estado = estadoMapping.dtotoEstado(estadoDto);
        return estadoRepository.save(estado);
    }

    @Override
    public boolean deleteEstado(Long id) {
        if (estadoRepository.existsById(id)){
            estadoRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<Estado> getAllEstado(){
        return estadoRepository.findAll();
    }

    @Override
    public Optional<Estado> findEstadoById(Long id) {
        return estadoRepository.findById(id);
    }

    @Override
    public Optional<EstadoDto> updateEstado(Long id, EstadoDto estadoDetails) {
        Optional<Estado> optionalEstado= estadoRepository.findById(id);
        if (optionalEstado.isPresent()) {
            Estado estado = optionalEstado.get();
            // Solo actualizás lo necesario
            estado.setNombreEstado(estadoDetails.getNombreEstado()); // suponiendo que el DTO tiene nombre
            Estado result = estadoRepository.save(estado);
            EstadoDto resultDTO = estadoMapping.estadoDto(result);
            return Optional.of(resultDTO);
        } else {
            return Optional.empty();
        }
    }
}
