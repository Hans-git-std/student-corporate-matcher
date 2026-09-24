package com.matcher.platform.controller;

import com.matcher.platform.security.mail.MailProviderStatusDto;
import com.matcher.platform.security.mail.ResilientMailRouter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "0. System Ping & Keep-Alive", description = "Ultra-lightweight public ping endpoint for cold-start prevention and uptime monitoring")
public class PingController {

    private static final long START_TIME_MS = System.currentTimeMillis();
    private final ResilientMailRouter mailRouter;

    public PingController(ResilientMailRouter mailRouter) {
        this.mailRouter = mailRouter;
    }

    @GetMapping({"/api/v1/ping", "/ping"})
    @Operation(summary = "Micro Ping Endpoint", description = "Instant zero-dependency response for keep-alive services, pingers, and health monitors.")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("pong", true);
        response.put("timestamp", Instant.now().toString());
        response.put("uptimeSeconds", (System.currentTimeMillis() - START_TIME_MS) / 1000);
        response.put("jvmUptimeMs", ManagementFactory.getRuntimeMXBean().getUptime());
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/api/v1/ping/mail", "/api/v1/ping/mail-status"})
    @Operation(summary = "Mail Providers Circuit Breaker & Health Status", description = "Public observability endpoint showing circuit breaker states (CLOSED/OPEN/HALF_OPEN), dispatch counts, and latency across Resend, Brevo, SendGrid, and SMTP.")
    public ResponseEntity<Map<String, Object>> getMailStatus() {
        List<MailProviderStatusDto> providers = mailRouter.getProvidersStatus();

        boolean anyConfigured = providers.stream().anyMatch(MailProviderStatusDto::configured);
        boolean anyClosed = providers.stream().anyMatch(p -> p.configured() && p.circuitState() == com.matcher.platform.security.mail.CircuitState.CLOSED);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("mailSystemHealth", !anyConfigured ? "NO_PROVIDER_CONFIGURED (DEV_CONSOLE_MODE)" : anyClosed ? "OPERATIONAL" : "ALL_CIRCUITS_DEGRADED");
        response.put("totalProviders", providers.size());
        response.put("providers", providers);

        return ResponseEntity.ok(response);
    }
}
