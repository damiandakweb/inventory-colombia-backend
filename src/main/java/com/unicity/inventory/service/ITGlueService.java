package com.unicity.inventory.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicity.inventory.mapping.IntegrationTypeMapping;
import com.unicity.inventory.mapping.IntegrationLocationMapping;
import com.unicity.inventory.repository.IntegrationTypeMappingRepository;
import com.unicity.inventory.repository.IntegrationLocationMappingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

@Service
public class ITGlueService {

    @Value("${ITGLUE_API_KEY}")
    private String apiKey;

    @Value("${ITGLUE_BASE_URL}")
    private String baseUrl;

    @Value("${ITGLUE_ORGANIZATION_ID}")
    private String organizationId;

    private final IntegrationTypeMappingRepository typeMappingRepository;
    private final IntegrationLocationMappingRepository locationMappingRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public ITGlueService(IntegrationTypeMappingRepository typeMappingRepository,
                         IntegrationLocationMappingRepository locationMappingRepository) {
        this.typeMappingRepository = typeMappingRepository;
        this.locationMappingRepository = locationMappingRepository;
    }

    // ✅ Refresca el cache de tipos desde ITGlue
    public void refreshTypeCache() {
        try {
            String response = get("/configuration_types");
            JsonNode root = objectMapper.readTree(response);
            JsonNode data = root.get("data");

            List<IntegrationTypeMapping> mappings = typeMappingRepository
                    .findAllByIntegration("ITGLUE");

            for (IntegrationTypeMapping mapping : mappings) {
                for (JsonNode type : data) {
                    String name = type.get("attributes").get("name").asText();
                    if (name.equalsIgnoreCase(mapping.getExternalTypeName())) {
                        mapping.setExternalTypeId(type.get("id").asText());
                        typeMappingRepository.save(mapping);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error refrescando cache de tipos ITGlue: " + e.getMessage());
        }
    }

    // ✅ Obtiene el type_id para una categoría
    public Long getTypeIdForCategoria(String nombreCategoria) {
        return typeMappingRepository
                .findByNombreCategoriaAndIntegration(nombreCategoria, "ITGLUE")
                .map(m -> Long.parseLong(m.getExternalTypeId()))
                .orElse(619980L); // fallback "Other"
    }

    // ✅ Obtiene el location_id para una ubicación
    public Long getLocationIdForUbicacion(String nombreUbicacion) {
        return locationMappingRepository
                .findByNombreUbicacionAndIntegration(nombreUbicacion, "ITGLUE")
                .map(m -> Long.parseLong(m.getExternalLocationId()))
                .orElse(11132425L); // fallback Bogotá
    }

    // ✅ Busca una configuration por serial number
    public Optional<Long> findBySerialNumber(String serialNumber) {
        try {
            String encodedSerial = java.net.URLEncoder.encode(serialNumber, "UTF-8");
            String response = get("/configurations?filter%5Bserial-number%5D="
                    + encodedSerial + "&filter%5Borganization-id%5D=" + organizationId);
            JsonNode root = objectMapper.readTree(response);
            JsonNode data = root.get("data");
            if (data != null && data.size() > 0) {
                return Optional.of(data.get(0).get("id").asLong());
            }
        } catch (Exception e) {
            System.err.println("Error buscando por serial: " + e.getMessage());
        }
        return Optional.empty();
    }

    // ✅ Crea una configuration en ITGlue
    public Optional<Long> createConfiguration(String name, String serialNumber,
                                              String manufacturerName, String modelName, Long typeId,
                                              Long statusId, String warrantyDate, String assetTag, Long locationId) {
        try {
            String body = buildConfigurationBody(null, name, serialNumber,
                    manufacturerName, modelName, typeId, statusId,
                    warrantyDate, assetTag, locationId);
            String response = post("/organizations/" + organizationId
                    + "/relationships/configurations", body);
            JsonNode root = objectMapper.readTree(response);
            return Optional.of(root.get("data").get("id").asLong());
        } catch (Exception e) {
            System.err.println("Error creando configuration: " + e.getMessage());
            return Optional.empty();
        }
    }

    // ✅ Actualiza una configuration en ITGlue
    public boolean updateConfiguration(Long itglueId, String name, String serialNumber,
                                       String manufacturerName, String modelName, Long typeId,
                                       Long statusId, String warrantyDate, String assetTag, Long locationId) {
        try {
            String body = buildConfigurationBody(itglueId, name, serialNumber,
                    manufacturerName, modelName, typeId, statusId,
                    warrantyDate, assetTag, locationId);
            patch("/organizations/" + organizationId
                    + "/relationships/configurations/" + itglueId, body);
            return true;
        } catch (Exception e) {
            System.err.println("Error actualizando configuration: " + e.getMessage());
            return false;
        }
    }

    // ✅ Archiva una configuration (baja)
    public boolean archiveConfiguration(Long itglueId) {
        try {
            String body = """
                {
                    "data": {
                        "type": "configurations",
                        "attributes": {
                            "archived": true
                        }
                    }
                }
                """;
            patch("/organizations/" + organizationId
                    + "/relationships/configurations/" + itglueId, body);
            return true;
        } catch (Exception e) {
            System.err.println("Error archivando configuration: " + e.getMessage());
            return false;
        }
    }

    // ✅ Construye el JSON body
    private String buildConfigurationBody(Long id, String name, String serialNumber,
                                          String manufacturerName, String modelName, Long typeId,
                                          Long statusId, String warrantyDate, String assetTag, Long locationId) {

        StringBuilder sb = new StringBuilder();
        sb.append("{\"data\":{\"type\":\"configurations\",\"attributes\":{");
        if (id != null) sb.append("\"id\":\"").append(id).append("\",");
        sb.append("\"name\":\"").append(name).append("\",");
        sb.append("\"serial-number\":\"").append(serialNumber != null ? serialNumber : "").append("\",");
        sb.append("\"asset-tag\":\"").append(assetTag != null ? assetTag : "").append("\",");
        if (manufacturerName != null) sb.append("\"manufacturer-name\":\"").append(manufacturerName).append("\",");
        if (modelName != null) sb.append("\"model-name\":\"").append(modelName).append("\",");
        if (warrantyDate != null) sb.append("\"warranty-expires-at\":\"").append(warrantyDate).append("\",");
        sb.append("\"configuration-type-id\":").append(typeId).append(",");
        sb.append("\"configuration-status-id\":").append(statusId).append(",");
        sb.append("\"location-id\":").append(locationId);
        sb.append("}}}");
        return sb.toString();
    }

    // ✅ HTTP helpers
    private String get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("x-api-key", apiKey)
                .header("Content-Type", "application/vnd.api+json")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private String post(String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("x-api-key", apiKey)
                .header("Content-Type", "application/vnd.api+json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());
        System.out.println("=== ITGlue POST " + path
                + " → " + response.statusCode());
        if (response.statusCode() >= 400) {
            System.err.println("=== ITGlue POST error body: " + response.body());
        }
        return response.body();
    }

    private void patch(String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("x-api-key", apiKey)
                .header("Content-Type", "application/vnd.api+json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());
        System.out.println("=== ITGlue PATCH " + path
                + " → " + response.statusCode());
        if (response.statusCode() >= 400) {
            System.err.println("=== ITGlue PATCH error body: " + response.body());
            System.err.println("=== ITGlue PATCH request body: " + body);
        }
    }
}