package com.matcher.platform.controller;

import com.matcher.platform.repository.SkillRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SkillRepository skillRepository;

    @Test
    @DisplayName("GET /api/v1/public/catalog/domains should return all domain catalogs")
    void testGetDomains() throws Exception {
        mockMvc.perform(get("/api/v1/public/catalog/domains"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(6))))
                .andExpect(jsonPath("$.data[*].domainCode", hasItems("CSE", "MECH", "ECE", "EE", "CHEM", "CIVIL")));
    }

    @Test
    @DisplayName("GET /api/v1/public/catalog/skills?query=spring should return matching skills")
    void testSearchSkills() throws Exception {
        when(skillRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/public/catalog/skills").param("query", "spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasItem("Spring Boot")));
    }

    @Test
    @DisplayName("GET /api/v1/public/catalog/subjects?domain=MECH should return Mechanical Engineering subjects")
    void testSearchSubjectsByDomain() throws Exception {
        mockMvc.perform(get("/api/v1/public/catalog/subjects").param("domain", "MECH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasItems("Thermodynamics", "Internal Combustion Engines", "Fluid Mechanics")));
    }
}
