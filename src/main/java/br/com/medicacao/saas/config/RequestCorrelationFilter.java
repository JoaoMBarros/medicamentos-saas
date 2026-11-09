package br.com.medicacao.saas.config;

import java.io.IOException;
import java.util.UUID;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestCorrelationFilter extends OncePerRequestFilter {

    static final String CORRELATION_HEADER = "X-Correlation-Id";
    static final String CORRELATION_MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain) throws ServletException, IOException {
        String correlationId = request.getHeader(CORRELATION_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        response.setHeader(CORRELATION_HEADER, correlationId);
        try (MDC.MDCCloseable ignored = MDC.putCloseable(CORRELATION_MDC_KEY, correlationId)) {
            filterChain.doFilter(request, response);
        }
    }
}
