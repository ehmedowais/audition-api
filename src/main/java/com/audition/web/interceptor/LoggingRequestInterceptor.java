package com.audition.web.interceptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggingRequestInterceptor.class);

    @Override
    public ClientHttpResponse intercept(
        HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        // Log the request details
        logRequest(request, body);

        // Execute the request
        ClientHttpResponse response = execution.execute(request, body);

        // Log the response details
        logResponse(response);

        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) {
        log.info("REST Request: {} {} Headers: {} Body: {}",
            request.getMethod(),
            request.getURI(),
            request.getHeaders(),
            new String(body, StandardCharsets.UTF_8));
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {

            String body = reader.lines().collect(Collectors.joining("\n"));

            log.info("REST Response: Status: {} {} Headers: {} Body: {}",
                response.getStatusCode(),
                response.getStatusText(),
                response.getHeaders(),
                body);
        }
    }
}

