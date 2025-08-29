package com.example.test;

// ...existing code...
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class WebhookService {
    private final RestTemplate restTemplate = new RestTemplate();

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            // Add delay to ensure proper startup
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 1. Send POST to generate webhook
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "John Doe");
        requestBody.put("regNo", "REG12347");
        requestBody.put("email", "john@example.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(url, entity,
                (Class<Map<String, Object>>) (Class<?>) Map.class);
        System.out.println("Webhook generation response: " + response);
        Map<String, Object> responseBody = response.getBody();
        if (response.getStatusCode() == HttpStatus.OK && responseBody != null) {
            System.out.println("Webhook response body: " + responseBody);
            String webhookUrl = (String) responseBody.get("webhook");
            String accessToken = (String) responseBody.get("accessToken");
            System.out.println("Using webhookUrl: " + webhookUrl);
            System.out.println("Using accessToken: " + accessToken);

            // 2. Solve the SQL problem (Question 1: highest salary not on 1st of month)
            // Try a simpler query first to test if format is the issue
            String finalQuery = "SELECT p.AMOUNT AS SALARY, CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, FLOOR(DATEDIFF(DATE(p.PAYMENT_TIME), e.DOB) / 365.25) AS AGE, d.DEPARTMENT_NAME FROM PAYMENTS p JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID WHERE DAY(DATE(p.PAYMENT_TIME)) <> 1 ORDER BY p.AMOUNT DESC LIMIT 1;";

            // 3. Submit the solution
            submitSolution(webhookUrl, accessToken, finalQuery);
        } else {
            System.err.println("Failed to generate webhook: " + response.getStatusCode());
            System.err.println("Response body: " + responseBody);
        }
    }

    private void submitSolution(String webhookUrl, String accessToken, String finalQuery) {
        try {
            // Add small delay before submission
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Submitting to webhook URL: " + webhookUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Accept", "application/json");

        Map<String, String> body = new HashMap<>();
        body.put("finalQuery", finalQuery);
        System.out.println("Request headers: " + headers);
        System.out.println("Request body: " + body);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, entity, String.class);
            System.out.println("SUCCESS! Submission response: " + response.getStatusCode());
            System.out.println("Submission response body: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Error submitting solution: " + e.getMessage());

            // Try alternative approach with different headers
            try {
                System.out.println("Trying alternative submission approach...");
                HttpHeaders altHeaders = new HttpHeaders();
                altHeaders.setContentType(MediaType.APPLICATION_JSON);
                altHeaders.set("Authorization", accessToken); // Without Bearer prefix

                HttpEntity<Map<String, String>> altEntity = new HttpEntity<>(body, altHeaders);
                ResponseEntity<String> altResponse = restTemplate.postForEntity(webhookUrl, altEntity, String.class);
                System.out.println("Alternative SUCCESS! Response: " + altResponse.getStatusCode());
                System.out.println("Alternative response body: " + altResponse.getBody());
            } catch (Exception altE) {
                System.err.println("Alternative approach also failed: " + altE.getMessage());
            }
        }
    }
}
