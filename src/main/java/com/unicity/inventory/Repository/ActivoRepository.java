package com.unicity.inventory.Repository;

import com.unicity.inventory.Models.Activo;
import com.unicity.inventory.Models.Categoria;
import com.unicity.inventory.Models.Usuario;
import org.springframework.data.jpa.repository.EntityGraph; // <-- IMPORTANTE AÑADIR ESTE IMPORT
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ActivoRepository extends JpaRepository<Activo, Long>, JpaSpecificationExecutor<Activo> {

    // ✅ ¡ESTA ES LA SOLUCIÓN DEFINITIVA!
    // Al sobreescribir findAll(), le aplicamos un "plan de carga".
    // Le decimos a JPA: "Cuando busques todos los activos, haz JOINs inmediatos
    // para traerme también los datos de categoria, estado y usuarioActual en la misma consulta".
    // Esto elimina el problema N+1 para esas entidades.
    @Override
    @EntityGraph(attributePaths = {"categoria", "estado", "usuarioActual"})
    List<Activo> findAll();

    // --- EL RESTO DE TUS MÉTODOS SE MANTIENEN IGUAL ---

    @Query("SELECT a FROM Activo a WHERE a.usuarioActual.idUsuario = :usuarioId")
    List<Activo> findActivosByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT new map(e.nombreEstado as name, count(a) as value) FROM Activo a JOIN a.estado e GROUP BY e.nombreEstado")
    List<Map<String, Object>> countActivosByEstado();

    @Query("SELECT a FROM Activo a WHERE a.usuarioActual.idUsuario = :usuarioId AND a.categoria.idCategoria = :categoriaId")
    List<Activo> findActivoByUsuarioAndCategoria(@Param("usuarioId") Long usuarioId, @Param("categoriaId") Long categoriaId);

    @Query("SELECT a FROM Activo a WHERE a.categoria.idCategoria = :categoriaId AND a.estado.idEstado IN :estadosIds AND a.usuarioActual IS NULL")
    List<Activo> findActivosDisponiblesPorCategoriaYEstados(
            @Param("categoriaId") Long categoriaId,
            @Param("estadosIds") List<Long> estadosIds
    );

    @Query("SELECT a FROM Activo a WHERE a.usuarioActual = :usuario AND a.categoria = :categoria")
    Optional<Activo> findByUsuarioAndCategoria(@Param("usuario") Usuario usuario, @Param("categoria") Categoria categoria);

    @Query("SELECT count(a) FROM Activo a WHERE a.categoria.nombreCategoria = :nombreCategoria AND a.estado.idEstado IN :estadosIds AND a.usuarioActual IS NULL")
    long countBackupByCategoriaAndEstado(
            @Param("nombreCategoria") String nombreCategoria,
            @Param("estadosIds") List<Long> estadosIds
    );
}