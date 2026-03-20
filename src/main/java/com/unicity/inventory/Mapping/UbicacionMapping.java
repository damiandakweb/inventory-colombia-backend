package com.unicity.inventory.Mapping;


import com.unicity.inventory.Models.Ubicacion;
import org.springframework.stereotype.Component;

@Component
public class UbicacionMapping {

        public UbicacionDto ubicacionDto(Ubicacion ubicacion) {
            return new UbicacionDto(ubicacion.getIdUbicacion(),ubicacion.getNombreUbicacion());
        }

        public Ubicacion dtotoUbicacion(UbicacionDto ubicacionDto) {
            return new Ubicacion(ubicacionDto.getIdUbicacion(),ubicacionDto.getNombreUbicacion());
        }
}
