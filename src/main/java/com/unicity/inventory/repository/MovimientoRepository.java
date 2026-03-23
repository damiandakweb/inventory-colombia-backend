package com.unicity.inventory.repository;

import com.unicity.inventory.models.Activo;
import com.unicity.inventory.models.Movimiento;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable; // ✅ Asegúrate que este import esté
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

        Optional<Movimiento> findFirstByActivoOrderByFechaMovimientoDesc(Activo activo);

        @Query("SELECT m FROM Movimiento m WHERE m.activo.idEquipo = :activoId ORDER BY m.fechaMovimiento DESC, m.idMovimiento DESC")
        List<Movimiento> findMovimientosByActivoId(@Param("activoId") Long activoId);

        @Query("SELECT m FROM Movimiento m " +
                "JOIN FETCH m.ubicacion " +
                "WHERE m.activo.idEquipo IN :activoIds " +
                "AND m.idMovimiento = (SELECT MAX(m2.idMovimiento) FROM Movimiento m2 WHERE m2.activo.idEquipo = m.activo.idEquipo)")
        List<Movimiento> findLatestMovementsForActivos(@Param("activoIds") List<Long> activoIds);

        // --- 👇 MÉTODOS RESTAURADOS PARA EL DASHBOARD ---

        // Este método devuelve una "página" de resultados, que podemos limitar a 5
        @Query("SELECT m FROM Movimiento m ORDER BY m.fechaMovimiento DESC, m.idMovimiento DESC")
        List<Movimiento> findLatestMovements(Pageable pageable);

        // Este es el método de ayuda que tu servicio llama directamente
        default List<Movimiento> findTop5() {
                // Pide la primera página (índice 0) con un tamaño de 5 elementos
                return findLatestMovements(PageRequest.of(0, 5));
        }
}