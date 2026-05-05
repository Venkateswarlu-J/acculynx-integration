package com.acculynx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

// Reads API key and base URL from application.properties.
// Provides shared HTTP headers and a RestTemplate bean

@Configuration
public class ApiConfig {

    @Value("${acculynx.base-url}")
    private String baseUrl;

    @Value("${acculynx.api-key}")
    private String apiKey;

    public String getBaseUrl() { return baseUrl; }


// Building standard headers required for every AccuLynx API call.
    public HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type",  "application/json");
        headers.set("Accept",        "application/json");
        return headers;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}