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
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "ID_ubicacion")
        private Long idUbicacion;
        @Column(name = "nombre_ubicacion")
        private String nombreUbicacion;
        @Column(name = "nombre_en")
        private String nombreEn;

        public Ubicacion(Long idUbicacion, String nombreUbicacion, String nombreEn) {
                this.idUbicacion = idUbicacion;
                this.nombreUbicacion = nombreUbicacion;
                this.nombreEn = nombreEn;
        }

        public Ubicacion() {

        }
}
