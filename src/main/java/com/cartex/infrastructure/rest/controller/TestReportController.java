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
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestReportController {

    private final RestTemplate restTemplate = new RestTemplate();

    record ReportRequest(String token, String reportid, String cutoffDate) {}

    @PostMapping("/report-json-bearer")
    public ResponseEntity<Map<String, Object>> jsonBearer(@RequestBody ReportRequest req) {
        String url = "https://reports.sadmin.net/api/generate_report";
        Map<String, Object> body = new HashMap<>();
        body.put("reportid", req.reportid());
        body.put("cutoffDate", req.cutoffDate());
        body.put("balanceType", -1);
        body.put("creditType", "ALL");
        body.put("advisorid", "ALL");
        body.put("branch", "ALL");
        body.put("clientGroup", "ALL");
        body.put("excludeRestrictedList", "NONE");
        body.put("numberOfDecimals", 2);
        body.put("identif", "ALL");
        body.put("names", "");
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.setBearerAuth(req.token());
        return executeAndReturn(url, new HttpEntity<>(body, h), "jsonBearer");
    }

    @PostMapping("/report-json-token-body")
    public ResponseEntity<Map<String, Object>> jsonTokenBody(@RequestBody ReportRequest req) {
        String url = "https://reports.sadmin.net/api/generate_report";
        Map<String, Object> body = new HashMap<>();
        body.put("token", req.token());
        body.put("reportid", req.reportid());
        body.put("cutoffDate", req.cutoffDate());
        body.put("balanceType", -1);
        body.put("creditType", "ALL");
        body.put("advisorid", "ALL");
        body.put("branch", "ALL");
        body.put("clientGroup", "ALL");
        body.put("excludeRestrictedList", "NONE");
        body.put("numberOfDecimals", 2);
        body.put("identif", "ALL");
        body.put("names", "");
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return executeAndReturn(url, new HttpEntity<>(body, h), "jsonTokenBody");
    }

    @PostMapping("/report-form-token-body")
    public ResponseEntity<Map<String, Object>> formTokenBody(@RequestBody ReportRequest req) {
        String url = "https://reports.sadmin.net/api/generate_report";
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("token", req.token());
        body.add("reportid", req.reportid());
        body.add("cutoffDate", req.cutoffDate());
        body.add("balanceType", "-1");
        body.add("creditType", "ALL");
        body.add("advisorid", "ALL");
        body.add("branch", "ALL");
        body.add("clientGroup", "ALL");
        body.add("excludeRestrictedList", "NONE");
        body.add("numberOfDecimals", "2");
        body.add("identif", "ALL");
        body.add("names", "");
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return executeAndReturn(url, new HttpEntity<>(body, h), "formTokenBody");
    }

    @PostMapping("/report-json-custom-header")
    public ResponseEntity<Map<String, Object>> jsonCustomHeader(@RequestBody ReportRequest req) {
        String url = "https://reports.sadmin.net/api/generate_report";
        Map<String, Object> body = new HashMap<>();
        body.put("reportid", req.reportid());
        body.put("cutoffDate", req.cutoffDate());
        body.put("balanceType", -1);
        body.put("creditType", "ALL");
        body.put("advisorid", "ALL");
        body.put("branch", "ALL");
        body.put("clientGroup", "ALL");
        body.put("excludeRestrictedList", "NONE");
        body.put("numberOfDecimals", 2);
        body.put("identif", "ALL");
        body.put("names", "");
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.add("token", req.token()); // raw token, no Bearer prefix
        return executeAndReturn(url, new HttpEntity<>(body, h), "jsonCustomHeader");
    }

    private ResponseEntity<Map<String, Object>> executeAndReturn(String url, HttpEntity<?> request, String mode) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            Map<String, Object> result = new HashMap<>();
            result.put("mode", mode);
            result.put("status", response.getStatusCode().value());
            result.put("bodySnippet", response.getBody() != null ? response.getBody().substring(0, Math.min(500, response.getBody().length())) : null);
            return ResponseEntity.ok(result);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("mode", mode);
            result.put("status", e.getStatusCode().value());
            result.put("errorBody", e.getResponseBodyAsString());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("mode", mode);
            result.put("error", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
}
