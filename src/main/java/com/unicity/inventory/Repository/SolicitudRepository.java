package com.unicity.inventory.Repository;

import com.unicity.inventory.Models.Categoria;
import com.unicity.inventory.Models.Solicitud;
import com.unicity.inventory.Models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface SolicitudRepository  extends JpaRepository<Solicitud, Long> {

    long countByEstadoSolicitud(String estado);

    Optional<Solicitud> findFirstByTipoSolicitudAndUsuarioAndCategoriaAndFechaSolicitudBetween(
            String tipo, Usuario usuario, Categoria categoria, LocalDate start, LocalDate end
    );

    Optional<Solicitud> findTopByTicketIdStartingWithOrderByTicketIdDesc(String prefijo);

    boolean existsByMarcaTemporalFuente(String marcaTemporalFuente);

}
