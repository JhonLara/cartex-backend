package com.cartex.infrastructure.rest.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestAuthController {

    private final RestTemplate restTemplate = new RestTemplate();

    record AuthRequest(String username, String password) {}

    @PostMapping("/auth-all")
    public ResponseEntity<Map<String, Object>> testAuthAll(@RequestBody AuthRequest req) {
        String url = "https://security.sadmin.net/security/login";
        Map<String, Object> allResults = new HashMap<>();

        // 1. JSON
        try {
            Map<String, String> jsonBody = new HashMap<>();
            jsonBody.put("username", req.username());
            jsonBody.put("password", req.password());
            HttpHeaders jHeaders = new HttpHeaders();
            jHeaders.setContentType(MediaType.APPLICATION_JSON);
            jHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Map<String, String>> jReq = new HttpEntity<>(jsonBody, jHeaders);
            allResults.put("json", doCall(url, jReq));
        } catch (Exception e) { allResults.put("json", Map.of("error", e.getMessage())); }

        // 2. Form URL encoded with MultiValueMap
        try {
            MultiValueMap<String, String> fBody = new LinkedMultiValueMap<>();
            fBody.add("username", req.username());
            fBody.add("password", req.password());
            HttpHeaders fHeaders = new HttpHeaders();
            fHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            fHeaders.setAccept(List.of(MediaType.ALL));
            HttpEntity<MultiValueMap<String, String>> fReq = new HttpEntity<>(fBody, fHeaders);
            allResults.put("form", doCall(url, fReq));
        } catch (Exception e) { allResults.put("form", Map.of("error", e.getMessage())); }

        // 3. Plain text form
        try {
            String tBody = "username=" + req.username() + "&password=" + req.password();
            HttpHeaders tHeaders = new HttpHeaders();
            tHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            tHeaders.setAccept(List.of(MediaType.ALL));
            HttpEntity<String> tReq = new HttpEntity<>(tBody, tHeaders);
            allResults.put("text", doCall(url, tReq));
        } catch (Exception e) { allResults.put("text", Map.of("error", e.getMessage())); }

        return ResponseEntity.ok(allResults);
    }

    private Map<String, Object> doCall(String url, HttpEntity<?> request) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            Map<String, Object> result = new HashMap<>();
            result.put("status", response.getStatusCode().value());
            result.put("body", response.getBody());
            return result;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", e.getStatusCode().value());
            result.put("errorBody", e.getResponseBodyAsString());
            return result;
        }
    }
}
