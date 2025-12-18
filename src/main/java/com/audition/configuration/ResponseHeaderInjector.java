package com.audition.configuration;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ResponseHeaderInjector extends OncePerRequestFilter {

    // TODO Inject openTelemetry trace and span Ids in the response headers.
    @Override
    public void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws java.io.IOException, jakarta.servlet.ServletException {

        Span span = Span.current();
        SpanContext context = span.getSpanContext();

        if (context.isValid()) {
            response.setHeader("X-Trace-Id", context.getTraceId());
            response.setHeader("X-Span-Id", context.getSpanId());
        }
        filterChain.doFilter(request, response);
    }
}
