package com.basilios.basilios.infra.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filtro de Rate Limiting para proteção contra ataques de brute force (OWASP A07).
 * Limita tentativas de login e reset de senha por IP.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> resetBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Rate limit apenas para POST em endpoints sensíveis
        if ("POST".equalsIgnoreCase(method)) {
            if ("/auth/login".equals(path)) {
                if (!tryConsume(loginBuckets, getClientIp(request), 10, Duration.ofMinutes(15))) {
                    sendTooManyRequests(response, "Muitas tentativas de login. Tente novamente em 15 minutos.");
                    return;
                }
            } else if ("/auth/esqueci-senha".equals(path)) {
                if (!tryConsume(resetBuckets, getClientIp(request), 3, Duration.ofMinutes(30))) {
                    sendTooManyRequests(response, "Muitas solicitações de reset de senha. Tente novamente em 30 minutos.");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean tryConsume(Map<String, Bucket> buckets, String key, int capacity, Duration refillDuration) {
        Bucket bucket = buckets.computeIfAbsent(key,
                k -> Bucket.builder()
                        .addLimit(Bandwidth.builder()
                                .capacity(capacity)
                                .refillIntervally(capacity, refillDuration)
                                .build())
                        .build()
        );
        return bucket.tryConsume(1);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void sendTooManyRequests(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"" + message + "\"}"
        );
    }
}
