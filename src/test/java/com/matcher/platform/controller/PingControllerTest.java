package com.matcher.platform.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/ping should return 200 OK with UP status without authentication")
    void testApiV1Ping() throws Exception {
        mockMvc.perform(get("/api/v1/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.pong").value(true));
    }

    @Test
    @DisplayName("GET /ping should return 200 OK with UP status without authentication")
    void testRootPing() throws Exception {
        mockMvc.perform(get("/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.pong").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/ping/mail should return 200 OK with mail provider circuit breaker status")
    void testMailStatusPing() throws Exception {
        mockMvc.perform(get("/api/v1/ping/mail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mailSystemHealth").exists())
                .andExpect(jsonPath("$.totalProviders").value(8))
                .andExpect(jsonPath("$.providers").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/ping/mail/test should return 200 OK and execute mail probe")
    void testMailProbe() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/ping/mail/test")
                        .param("to", "testprobe@domain.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipient").value("testprobe@domain.com"))
                .andExpect(jsonPath("$.delivered").isBoolean())
                .andExpect(jsonPath("$.providers").isArray());
    }
}
