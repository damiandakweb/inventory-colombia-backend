package com.unicity.inventory.Service;

import com.unicity.inventory.Mapping.SolicitudDto;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvReaderService {

    private static final String CSV_URL = "https://docs.google.com/spreadsheets/d/e/2PACX-1vTBdJiKq7GEMoaynn7ENpqbVeDewqPJpblJ63dNtCY2UuBZtaiOv58sOhi0OZYCOpfcE1mCwwsxbgUz/pub?gid=2068752320&single=true&output=csv";

    public List<SolicitudDto> leerSolicitudesDesdeCsv() {
        List<SolicitudDto> solicitudes = new ArrayList<>();

        try {
            // USAMOS URLConnection para añadir un User-Agent y evitar el Error 400
            java.net.URLConnection connection = new java.net.URL(CSV_URL).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            try (Reader reader = new InputStreamReader(connection.getInputStream())) {
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
                        .withTrim());

                List<CSVRecord> records = csvParser.getRecords();
                System.out.println("INFO: Filas detectadas en el CSV: " + records.size());

                for (CSVRecord csvRecord : records) {
                    try {
                        SolicitudDto dto = new SolicitudDto();
                        dto.setNombreUsuario(csvRecord.get("Nombre_Solicitante"));
                        dto.setTipoSolicitud(csvRecord.get("Tipo_Solicitud"));
                        dto.setNombreCategoria(csvRecord.get("Categoria_Solicitada"));
                        dto.setMarcaTemporalFuente(csvRecord.get("marca_temporal_fuente"));
                        solicitudes.add(dto);
                    } catch (Exception e) {
                        System.err.println("WARN: Error procesando una fila específica: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("ERROR crítico al leer CSV: " + e.getMessage());
        }
        return solicitudes;
    }
}