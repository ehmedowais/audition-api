package com.audition.web.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;

@ExtendWith(MockitoExtension.class)
class LoggingRequestInterceptorTest {

    private transient LoggingRequestInterceptor interceptor;

    @Mock
    private transient ClientHttpRequestExecution execution;

    @Mock
    private transient HttpRequest request;

    @BeforeEach
    void setUp() {
        interceptor = new LoggingRequestInterceptor();
    }

    @Test
    void intercept_shouldLogRequestAndResponse_andReturnResponse() throws IOException {
        byte[] requestBody = "request-body".getBytes(StandardCharsets.UTF_8);

        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getURI()).thenReturn(URI.create("https://sampleposts.com/api"));
        when(request.getHeaders()).thenReturn(new HttpHeaders());

        try (ClientHttpResponse response =
            new MockClientHttpResponse(
                "response-body".getBytes(StandardCharsets.UTF_8),
                HttpStatus.OK
            )) {

            when(execution.execute(request, requestBody)).thenReturn(response);

            try (ClientHttpResponse result =
                interceptor.intercept(request, requestBody, execution)) {

                assertNotNull(result);
                assertEquals(HttpStatus.OK, result.getStatusCode());

                verify(execution).execute(request, requestBody);
            }
        }
    }

    @Test
    void intercept_shouldHandleEmptyResponseBody() throws IOException {
        byte[] requestBody = new byte[0];

        when(request.getMethod()).thenReturn(HttpMethod.POST);
        when(request.getURI()).thenReturn(URI.create("https://sampleposts.com/api"));
        when(request.getHeaders()).thenReturn(new HttpHeaders());

        try (ClientHttpResponse response =
            new MockClientHttpResponse(
                new byte[0],
                HttpStatus.NO_CONTENT
            )) {

            when(execution.execute(request, requestBody)).thenReturn(response);

            try (ClientHttpResponse result =
                interceptor.intercept(request, requestBody, execution)) {

                assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
                verify(execution).execute(request, requestBody);
            }
        }
    }

    @Test
    void intercept_shouldPropagateIOException_fromExecution() throws IOException {
        byte[] requestBody = "data".getBytes(StandardCharsets.UTF_8);

        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getURI()).thenReturn(URI.create("https://sampleposts.com"));
        when(request.getHeaders()).thenReturn(new HttpHeaders());

        when(execution.execute(request, requestBody))
            .thenThrow(new IOException("connection failed"));

        assertThrows(IOException.class, () ->
            interceptor.intercept(request, requestBody, execution)
        );

        verify(execution).execute(request, requestBody);
    }

}
