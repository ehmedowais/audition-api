package com.audition.configuration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class ResponseHeaderInjectorTest {

    private transient ResponseHeaderInjector filter;

    @Mock
    private transient FilterChain filterChain;

    private transient HttpServletRequest request;
    private transient MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new ResponseHeaderInjector();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
        justification = "Response headers are set by filter in test")
    void shouldAddTraceAndSpanHeaders_whenSpanContextIsValid() throws Exception {
        Span span = mock(Span.class);
        SpanContext context = mock(SpanContext.class);

        when(context.isValid()).thenReturn(true);
        when(context.getTraceId()).thenReturn("trace-id-123");
        when(context.getSpanId()).thenReturn("span-id-456");
        when(span.getSpanContext()).thenReturn(context);

        try (MockedStatic<Span> spanStatic = mockStatic(Span.class)) {
            spanStatic.when(Span::current).thenReturn(span);

            filter.doFilterInternal(request, response, filterChain);
        }

        // Assert headers
        String traceId = Objects.requireNonNull(
            response.getHeader("X-Trace-Id"), "traceId should not be null");
        String spanId = Objects.requireNonNull(
            response.getHeader("X-Span-Id"), "spanId should not be null");
        assert traceId.equals("trace-id-123");
        assert spanId.equals("span-id-456");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAddHeaders_whenSpanContextIsInvalid() throws Exception {
        Span span = mock(Span.class);
        SpanContext context = mock(SpanContext.class);

        when(context.isValid()).thenReturn(false);
        when(span.getSpanContext()).thenReturn(context);

        try (MockedStatic<Span> spanStatic = mockStatic(Span.class)) {
            spanStatic.when(Span::current).thenReturn(span);

            filter.doFilterInternal(request, response, filterChain);
        }

        // Assert headers are absent
        assert response.getHeader("X-Trace-Id") == null;
        assert response.getHeader("X-Span-Id") == null;

        verify(filterChain).doFilter(request, response);
    }
}
