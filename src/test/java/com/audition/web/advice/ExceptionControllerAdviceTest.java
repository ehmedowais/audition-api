package com.audition.web.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.client.HttpClientErrorException;

class ExceptionControllerAdviceTest {

    private transient ExceptionControllerAdvice advice;
    private transient AuditionLogger auditionLogger;

    @BeforeEach
    void setUp() {
        advice = new ExceptionControllerAdvice();
        auditionLogger = mock(AuditionLogger.class);
        // Inject the mocked logger into the private field
        ReflectionTestUtils.setField(advice, "logger", auditionLogger);
    }

    @Test
    void testHandleHttpClientException() {
        HttpClientErrorException exception = new HttpClientErrorException(NOT_FOUND, "Resource not found");

        ProblemDetail result = advice.handleHttpClientException(exception);

        assertEquals(NOT_FOUND.value(), result.getStatus());
        assertEquals("404 Resource not found", result.getDetail());
        assertEquals(ExceptionControllerAdvice.DEFAULT_TITLE, result.getTitle());
    }

    @Test
    void testHandleSystemExceptionWithValidStatus() {
        String title = "Custom System Error";
        String message = "System failure occurred";
        SystemException exception = new SystemException(message, title, 400);

        ProblemDetail result = advice.handleSystemException(exception);

        assertEquals(BAD_REQUEST.value(), result.getStatus());
        assertEquals(message, result.getDetail());
        assertEquals(title, result.getTitle());
    }

    @Test
    void testHandleSystemExceptionWithInvalidStatus() {
        // Status code 1000 does not exist, till 999 HttpStatus would return the number
        SystemException exception = new SystemException("Invalid status", "Error", 999);

        ProblemDetail result = advice.handleSystemException(exception);

        assertEquals(INTERNAL_SERVER_ERROR.value(), result.getStatus());
    }

    @Test
    void testHandleMainExceptionWithHttpRequestMethodNotSupported() {
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("POST");

        ProblemDetail result = advice.handleMainException(exception);

        assertEquals(METHOD_NOT_ALLOWED.value(), result.getStatus());
        assertEquals(ExceptionControllerAdvice.DEFAULT_TITLE, result.getTitle());
    }

    @Test
    void testHandleMainExceptionWithGenericException() {
        Exception exception = new RuntimeException("Unexpected error");

        ProblemDetail result = advice.handleMainException(exception);

        assertEquals(INTERNAL_SERVER_ERROR.value(), result.getStatus());
        assertEquals("Unexpected error", result.getDetail());
    }

    @Test
    void testGetMessageFromExceptionReturnsDefaultWhenNull() {
        // Testing private logic via the public handler
        Exception exception = new Exception((String) null);

        ProblemDetail result = advice.handleMainException(exception);

        // Should return the DEFAULT_MESSAGE defined in the class
        assertEquals("API Error occurred. Please contact support or administrator.", result.getDetail());
    }
}