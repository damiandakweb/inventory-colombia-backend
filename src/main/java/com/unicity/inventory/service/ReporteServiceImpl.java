package com.unicity.inventory.service;

import com.unicity.inventory.mapping.ReporteRequest;
import com.unicity.inventory.models.Activo;
import com.unicity.inventory.repository.ActivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private final ActivoRepository activoRepository;

    @Override
    public String generarCsvDinamico(ReporteRequest request) {
        // 1. Construir la especificación de la consulta a partir de los filtros
        Specification<Activo> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request.getFiltros() != null) {
                request.getFiltros().forEach((campo, valor) -> {
                    if (valor != null && !valor.toString().isEmpty()) {
                        // Lógica para filtros existentes
                        if (campo.equals("idCategoria")) {
                            predicates.add(criteriaBuilder.equal(root.get("categoria").get("idCategoria"), valor));
                        }
                        if (campo.equals("idEstado")) {
                            predicates.add(criteriaBuilder.equal(root.get("estado").get("idEstado"), valor));
                        }
                        if (campo.equals("idUsuarioActual")) {
                            predicates.add(criteriaBuilder.equal(root.get("usuarioActual").get("idUsuario"), valor));
                        }

                        // ✅ =======================================================
                        // ✅ INICIO DE LA CORRECCIÓN: Añadir lógica para el filtro "pais"
                        // ✅ =======================================================
                        if (campo.equals("pais")) {
                            predicates.add(criteriaBuilder.equal(root.get("pais"), valor));
                        }
                        // =======================================================
                        // ✅ FIN DE LA CORRECCIÓN
                        // =======================================================
                    }
                });
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 2. Ejecutar la consulta dinámica
        List<Activo> activosFiltrados = activoRepository.findAll(spec);

        // 3. Construir el archivo CSV
        StringBuilder csvBuilder = new StringBuilder();
        // Encabezado
        csvBuilder.append(String.join(",", request.getColumnas())).append("\n");

        // Filas
        for (Activo activo : activosFiltrados) {
            List<String> valoresFila = new ArrayList<>();
            for (String columna : request.getColumnas()) {
                valoresFila.add(getValorDeColumna(activo, columna));
            }
            csvBuilder.append(String.join(",", valoresFila)).append("\n");
        }

        return csvBuilder.toString();
    }

    // Método de ayuda para obtener el valor de un campo por su nombre
    private String getValorDeColumna(Activo activo, String nombreColumna) {
        // Usamos un switch para mayor claridad y rendimiento
        switch (nombreColumna) {
            case "ID Equipo": return String.valueOf(activo.getIdEquipo());
            case "Etiqueta Inventario": return activo.getEtiquetaInventario() != null ? activo.getEtiquetaInventario() : "";
            case "Numero de Serie": return activo.getNumeroDeSerie() != null ? activo.getNumeroDeSerie() : "";
            case "Categoria": return activo.getCategoria() != null ? activo.getCategoria().getNombreCategoria() : "";
            case "Estado": return activo.getEstado() != null ? activo.getEstado().getNombreEstado() : "";
            case "Usuario Actual": return activo.getUsuarioActual() != null ? activo.getUsuarioActual().getNombre() : "Sin Asignar";
            case "Fecha de Compra": return activo.getFechaCompra() != null ? activo.getFechaCompra().toString() : "N/A";
            case "Fecha Fin Garantia": return activo.getFechaGarantia() != null ? activo.getFechaGarantia().toString() : "N/A";
            case "Marca": return activo.getMarca() != null ? activo.getMarca() : "";
            case "Modelo": return activo.getModelo() != null ? activo.getModelo() : "";

            // ✅ =======================================================
            // ✅ INICIO DE LA CORRECCIÓN: Añadir lógica para la columna "Pais"
            // ✅ =======================================================
            case "Pais": return activo.getPais() != null ? activo.getPais() : "";
            // =======================================================
            // ✅ FIN DE LA CORRECCIÓN
            // =======================================================

            default: return "";
        }
    }
}
