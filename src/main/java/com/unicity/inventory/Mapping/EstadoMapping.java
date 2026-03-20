package com.unicity.inventory.Mapping;

import com.unicity.inventory.Models.Estado;
import org.springframework.stereotype.Component;

@Component
public class EstadoMapping {


    public EstadoDto estadoDto(Estado estado) {

        return new EstadoDto(estado.getIdEstado(),estado.getNombreEstado());
    }
    public Estado dtotoEstado(EstadoDto estadoDto) {
        return new Estado(estadoDto.getIdEstado(),estadoDto.getNombreEstado());
    }
}
