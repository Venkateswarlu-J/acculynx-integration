package com.acculynx.service;

import com.acculynx.config.ApiConfig;
import com.acculynx.config.ApiEndpoints;
import com.acculynx.model.ContactRequest;
import com.acculynx.model.JobRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class AccuLynxClient {

    private final RestTemplate restTemplate;
    private final ApiConfig    apiConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AccuLynxClient(RestTemplate restTemplate, ApiConfig apiConfig) {
        this.restTemplate = restTemplate;
        this.apiConfig    = apiConfig;
    }

    // ── STEP 1 — Create Contact ──
    public String createContact(String firstName, String lastName,
                                String phone, String email) {

        String url = apiConfig.getBaseUrl() + ApiEndpoints.CONTACTS;
        ContactRequest body = new ContactRequest(firstName, lastName, phone, email);
        HttpEntity<ContactRequest> request = new HttpEntity<>(body, apiConfig.buildHeaders());

        System.out.println("\n>>> Step 1: Creating Contact...");

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.POST, request, String.class);

//            System.out.println("    Raw Response: " + response.getBody());
            String contactId = extractId(response.getBody(), "id", "contactId", "ContactId");
            System.out.println("  ✔ Contact created successfully. Contact ID: " + contactId);
            return contactId;

        } catch (HttpClientErrorException e) {
            throw new RuntimeException(
                    "Create Contact failed [HTTP " + e.getStatusCode() + "]: "
                            + parseErrorMessage(e.getResponseBodyAsString()));
        } catch (HttpServerErrorException e) {
            throw new RuntimeException(
                    "AccuLynx server error [HTTP " + e.getStatusCode() + "]");
        }
    }

    // ── STEP 2 — Create Job ──
    public String createJob(String contactId,
                            String street, String city,
                            String state,  String zipCode) {

        String url = apiConfig.getBaseUrl() + ApiEndpoints.JOBS;
        JobRequest body = new JobRequest(contactId, street, city, state, zipCode);
        HttpEntity<JobRequest> request = new HttpEntity<>(body, apiConfig.buildHeaders());

        System.out.println("\n>>> Step 2: Creating Job linked to Contact ID: " + contactId);

        try {
            // ── Debug logs — shows exact URL and JSON sent to API ──
//            System.out.println("    Sending URL : "+url);
//            System.out.println("    Sending Body: "+objectMapper.writeValueAsString(body));

            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.POST, request, String.class);

//            System.out.println("    Raw Response: " + response.getBody());
            String jobId = extractId(response.getBody(), "id", "jobId", "JobId");
            System.out.println("  ✔ Job created successfully. Job ID: " + jobId);
            return jobId;

        } catch (HttpClientErrorException e) {
            throw new RuntimeException(
                    "Create Job failed [HTTP " + e.getStatusCode() + "]: "
                            + parseErrorMessage(e.getResponseBodyAsString()));
        } catch (HttpServerErrorException e) {
            throw new RuntimeException(
                    "AccuLynx server error [HTTP " + e.getStatusCode() + "]");
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error: " + e.getMessage());
        }
    }

    //accessing the data using contactId
    public JsonNode getContact(String contactId) throws Exception {
        String url = apiConfig.getBaseUrl() + ApiEndpoints.CONTACTS + "/" + contactId;
        HttpEntity<Void> request = new HttpEntity<>(apiConfig.buildHeaders());

        ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.GET, request, String.class);

//        System.out.println("[WEBHOOK] Contact API Response: " + response.getBody());
        return objectMapper.readTree(response.getBody());
    }
    //The data of email and phone is in that link so we are accessing from it
    public JsonNode getByUrl(String url) throws Exception {
        HttpEntity<Void> request = new HttpEntity<>(apiConfig.buildHeaders());
        ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.GET, request, String.class);
//        System.out.println("[WEBHOOK] Sub-resource response: " + response.getBody());
        return objectMapper.readTree(response.getBody());
    }

    // ── Extract ID from JSON response ──
    private String extractId(String responseBody, String... fieldNames) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            for (String field : fieldNames) {
                JsonNode node = root.get(field);
                if (node != null && !node.isNull() && !node.asText().isBlank()) {
                    return node.asText();
                }
            }
            throw new RuntimeException(
                    "ID not found. Fields tried: " + String.join(", ", fieldNames)
                            + " | Body: " + responseBody);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response: " + e.getMessage());
        }
    }

    // ── Extract error message from HTTP response ──
    private String parseErrorMessage(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            for (String field : new String[]{"message", "error", "title", "detail"}) {
                JsonNode node = root.get(field);
                if (node != null && !node.isNull()) return node.asText();
            }
            return responseBody;
        } catch (Exception e) {
            return responseBody;
        }
    }
}