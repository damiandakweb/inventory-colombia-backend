package com.unicity.inventory.models;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Data
@Table(name = "Ubicacion")
@Getter
@Setter
public class Ubicacion {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY) // <-- AÑADE ESTA LÍNEA
        @Column(name = "ID_ubicacion")
        private Long idUbicacion;
        @Column(name = "nombre_ubicacion")
        private String nombreUbicacion;

        public Ubicacion(Long idUbicacion, String nombreUbicacion) {
                this.idUbicacion = idUbicacion;
                this.nombreUbicacion = nombreUbicacion;
        }

        public Ubicacion() {

        }
}
