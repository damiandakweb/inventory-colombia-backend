package com.unicity.inventory.mapping;

import com.unicity.inventory.models.Estado;
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
