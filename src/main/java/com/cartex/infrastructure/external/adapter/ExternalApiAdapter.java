package com.cartex.infrastructure.external.adapter;

import com.cartex.domain.port.ExternalApiPort;
import com.cartex.infrastructure.external.config.ExternalApiProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalApiAdapter implements ExternalApiPort {

    private final ExternalApiProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, CachedToken> tokenCache = new ConcurrentHashMap<>();

    private record CachedToken(String token, long expiryTime) {}

    @Override
    public String authenticateAndGetToken(String username, String password) {
        CachedToken cached = tokenCache.get(username);
        if (cached != null && System.currentTimeMillis() < cached.expiryTime()) {
            log.debug("Reusing cached external API token for {}", username);
            return cached.token();
        }

        String authUrl = properties.getAuthUrl();
        log.info("Authenticating with external API at: {}", authUrl);

        // SAdmin requires form-urlencoded for auth (not JSON)
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("username", username);
        requestBody.add("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);
        try {
            ResponseEntity<String> rawResponse = restTemplate.exchange(authUrl, HttpMethod.POST, request, String.class);
            log.info("Auth raw response status: {}", rawResponse.getStatusCode());

            if (!rawResponse.getStatusCode().is2xxSuccessful() || rawResponse.getBody() == null) {
                throw new RuntimeException("Auth failed with status: " + rawResponse.getStatusCode() + ", body: " + rawResponse.getBody());
            }

            Map<String, Object> responseBody = objectMapper.readValue(rawResponse.getBody(), new TypeReference<Map<String, Object>>() {});
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            if (data == null) {
                throw new RuntimeException("External API auth response missing 'data' field. Body: " + rawResponse.getBody());
            }
            String token = (String) data.get("token");
            if (token == null) {
                throw new RuntimeException("External API auth response missing 'token' field. Body: " + rawResponse.getBody());
            }

            Long expiryMs = extractExpiryFromJwt(token);
            long expiryTime = expiryMs != null ? expiryMs : System.currentTimeMillis() + 3_600_000L;
            tokenCache.put(username, new CachedToken(token, expiryTime));
            log.info("External API token acquired for {}, expires at {}", username, expiryTime);
            return token;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Auth failed with status {}. Response body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Auth failed: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Auth processing error: {}", e.getMessage(), e);
            throw new RuntimeException("Auth processing failed: " + e.getMessage(), e);
        }
    }

    private Long extractExpiryFromJwt(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return null;
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            Map<String, Object> payload = objectMapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {});
            Number exp = (Number) payload.get("exp");
            return exp != null ? exp.longValue() * 1000L : null;
        } catch (Exception e) {
            log.warn("Could not extract expiry from JWT, using default 1h: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public <T> T executeWithToken(String endpoint, Object requestBody, Class<T> responseType, String token) {
        String url = endpoint.startsWith("http") ? endpoint : properties.getReportsBaseUrl() + endpoint;
        log.info("External API call to: {}", url);

        // Serialize body to JSON string explicitly to avoid chunked encoding issues with IIS/Django
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize request body", e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentLength(jsonBody.getBytes().length);

        HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);
        try {
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, request, responseType);
            log.info("External API response status: {}", response.getStatusCode());
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
            throw new RuntimeException("External API call failed: " + response.getStatusCode());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("External API call failed with status {}. Response body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("External API call failed: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        }
    }
}
