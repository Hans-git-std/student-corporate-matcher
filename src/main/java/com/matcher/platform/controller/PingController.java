package com.matcher.platform.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Tag(name = "0. System Ping & Keep-Alive", description = "Ultra-lightweight public ping endpoint for cold-start prevention and uptime monitoring")
public class PingController {

    private static final long START_TIME_MS = System.currentTimeMillis();

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
}
