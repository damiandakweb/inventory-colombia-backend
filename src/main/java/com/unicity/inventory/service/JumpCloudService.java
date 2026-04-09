package com.unicity.inventory.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Optional;

@Service
public class JumpCloudService {

    @Value("${JUMPCLOUD_CLIENT_ID}")
    private String clientId;

    @Value("${JUMPCLOUD_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${JUMPCLOUD_BASE_URL}")
    private String baseUrl;

    @Value("${JUMPCLOUD_AUTH_URL}")
    private String authUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String cachedToken = null;
    private long tokenExpiryTime = 0;

    // ✅ Obtiene el token OAuth — lo renueva si expiró
    private String getToken() throws Exception {
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return cachedToken;
        }

        String credentials = clientId + ":" + clientSecret;
        String base64Credentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authUrl))
                .header("Authorization", "Basic " + base64Credentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "scope=api&grant_type=client_credentials"))
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());
        JsonNode root = objectMapper.readTree(response.body());

        cachedToken = root.get("access_token").asText();
        // Token expira en 3600s — lo renovamos 5 minutos antes
        long expiresIn = root.has("expires_in")
                ? root.get("expires_in").asLong() : 3600L;
        tokenExpiryTime = System.currentTimeMillis()
                + ((expiresIn - 300) * 1000);

        System.out.println("Token JumpCloud obtenido correctamente.");
        return cachedToken;
    }

    // ✅ Busca un sistema por serial number
    public Optional<String> findBySerialNumber(String serialNumber) {
        try {
            System.out.println("=== Buscando en JumpCloud serial: " + serialNumber);
            String encodedSerial = java.net.URLEncoder.encode(
                    serialNumber, "UTF-8");
            String response = get("/api/systems?filter=serialNumber:eq:"
                    + encodedSerial + "&limit=1");
            System.out.println("=== Respuesta JumpCloud: " + response);
            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.get("results");
            if (results != null && results.size() > 0) {
                return Optional.of(results.get(0).get("_id").asText());
            }
        } catch (Exception e) {
            System.err.println("Error buscando sistema JumpCloud por serial: "
                    + e.getMessage());
        }
        return Optional.empty();
    }

    // ✅ Actualiza el displayName de un sistema
    public boolean updateSystem(String jumpcloudId, String displayName) {
        try {
            String body = "{\"displayName\":\"" + displayName + "\"}";
            put("/api/systems/" + jumpcloudId, body);
            return true;
        } catch (Exception e) {
            System.err.println("Error actualizando sistema JumpCloud "
                    + jumpcloudId + ": " + e.getMessage());
            return false;
        }
    }

    // ✅ HTTP helpers
    private String get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Authorization", "Bearer " + getToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .GET()
                .build();
        return httpClient.send(request,
                HttpResponse.BodyHandlers.ofString()).body();
    }

    private void put(String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Authorization", "Bearer " + getToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}