package com.matcher.platform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Apply rate limit specifically to auth endpoints or general sensitive routes
        if (path.startsWith("/api/v1/auth/")) {
            String clientIp = getClientIp(request);
            long currentMinute = System.currentTimeMillis() / 60000;
            String key = clientIp + ":" + currentMinute;

            RequestCounter counter = requestCounts.computeIfAbsent(key, k -> new RequestCounter());
            if (counter.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(String.format(
                        "{\"timestamp\":\"%s\",\"status\":429,\"message\":\"Too many requests. Please slow down and try again in a minute.\"}",
                        Instant.now()
                ));
                return;
            }

            // Periodically clean up stale minute windows
            if (requestCounts.size() > 5000) {
                requestCounts.entrySet().removeIf(entry -> {
                    String[] parts = entry.getKey().split(":");
                    if (parts.length > 1) {
                        try {
                            long min = Long.parseLong(parts[1]);
                            return min < currentMinute;
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    return false;
                });
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    private static class RequestCounter {
        private final AtomicInteger count = new AtomicInteger(0);

        int incrementAndGet() {
            return count.incrementAndGet();
        }
    }
}
