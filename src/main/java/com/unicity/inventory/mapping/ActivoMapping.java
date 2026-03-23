package com.unicity.inventory.mapping;

import com.unicity.inventory.models.Activo;
// Ya no necesitamos MovimientoRepository aquí
import org.springframework.stereotype.Component;
// import java.util.Optional; // Ya no se usa
import java.util.Collections; // Importar para lista vacía

@Component
public class ActivoMapping {

    // El constructor ahora es simple
    public ActivoMapping() {}

    public ActivoDto toDto(Activo activo) {
        if (activo == null) return null;
        ActivoDto dto = new ActivoDto();
        dto.setIdEquipo(activo.getIdEquipo());
        dto.setNumeroDeSerie(activo.getNumeroDeSerie());
        dto.setFechaGarantia(activo.getFechaGarantia());
        dto.setFechaCompra(activo.getFechaCompra());
        dto.setEtiquetaInventario(activo.getEtiquetaInventario());
        dto.setMarca(activo.getMarca());
        dto.setModelo(activo.getModelo());
        dto.setPais(activo.getPais());

        if (activo.getEstado() != null) {
            dto.setNombreEstado(activo.getEstado().getNombreEstado());
            dto.setIdEstado(activo.getEstado().getIdEstado());
        }
        if (activo.getCategoria() != null) {
            dto.setNombreCategoria(activo.getCategoria().getNombreCategoria());
            dto.setIdCategoria(activo.getCategoria().getIdCategoria());
        }
        if (activo.getUsuarioActual() != null) {
            dto.setNombreUsuarioActual(activo.getUsuarioActual().getNombre());
            dto.setIdUsuarioActual(activo.getUsuarioActual().getIdUsuario());
        }

        // La ubicación la asigna el servicio.

        // --- IMPORTANTE: NO mapeamos los relacionados aquí ---
        // Dejamos la lista vacía por defecto. Si se necesitaran en el futuro
        // para la tabla principal, habría que optimizarlo de otra forma,
        // pero para el tooltip del frontend, esto es lo más rápido.
        // Si quieres que el tooltip siga funcionando como antes SIN modificar el frontend,
        // necesitaríamos añadir "activosRelacionados" al @EntityGraph, pero eso
        // podría traer demasiados datos si las relaciones son muchas.
        // Por ahora, prioricemos la velocidad de carga de la lista.
        dto.setActivosRelacionados(Collections.emptyList()); // Devolver lista vacía

        return dto;
    }

    // toSimpleDto se mantiene igual, es útil para otros contextos
    public ActivoDto toSimpleDto(Activo activo) {
        if (activo == null) return null;
        ActivoDto dto = new ActivoDto();
        dto.setIdEquipo(activo.getIdEquipo());
        dto.setEtiquetaInventario(activo.getEtiquetaInventario());
        dto.setMarca(activo.getMarca());
        dto.setModelo(activo.getModelo());
        dto.setPais(activo.getPais());
        return dto;
    }

    // toEntity se mantiene igual
    public Activo toEntity(ActivoDto dto) {
        if (dto == null) return null;
        Activo activo = new Activo();
        // ... (resto de asignaciones igual)
        activo.setNumeroDeSerie(dto.getNumeroDeSerie());
        activo.setEtiquetaInventario(dto.getEtiquetaInventario());
        activo.setFechaCompra(dto.getFechaCompra());
        activo.setFechaGarantia(dto.getFechaGarantia());
        activo.setMarca(dto.getMarca());
        activo.setModelo(dto.getModelo());
        activo.setPais(dto.getPais());
        return activo;
    }
}