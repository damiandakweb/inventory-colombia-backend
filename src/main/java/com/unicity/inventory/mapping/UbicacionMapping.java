package com.unicity.inventory.mapping;


import com.unicity.inventory.models.Ubicacion;
import org.springframework.stereotype.Component;

@Component
public class UbicacionMapping {

        public UbicacionDto ubicacionDto(Ubicacion ubicacion) {
            return new UbicacionDto(ubicacion.getIdUbicacion(),ubicacion.getNombreUbicacion(),ubicacion.getNombreEn());
        }

        public Ubicacion dtotoUbicacion(UbicacionDto ubicacionDto) {
            return new Ubicacion(ubicacionDto.getIdUbicacion(),ubicacionDto.getNombreUbicacion(),ubicacionDto.getNombreEn());
        }
}
