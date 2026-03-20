package com.unicity.inventory.Models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "Solicitud")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_solicitud")
    private Long idSolicitud;

    @Column(name = "tipo_solicitud")
    private String tipoSolicitud;

    @Column(name = "fecha_solicitud")
    private LocalDate fechaSolicitud;

    @Column(name = "estado_solicitud")
    private String estadoSolicitud;

    @ManyToOne(fetch = FetchType.LAZY) // Usar LAZY es una buena práctica para el rendimiento
    @JoinColumn(name = "ID_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_categoria")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_movimiento") // <-- Asegúrate de que esta relación exista
    private Movimiento movimiento;

    @Column(name = "marca_temporal_fuente", unique = true)
    private String marcaTemporalFuente;

    @Column(name = "ticket_id", unique = true)
    private String ticketId;


}