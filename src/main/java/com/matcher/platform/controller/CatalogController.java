package com.matcher.platform.controller;

import com.matcher.platform.dto.common.ApiResponse;
import com.matcher.platform.entity.Skill;
import com.matcher.platform.repository.SkillRepository;
import com.matcher.platform.util.CatalogData;
import com.matcher.platform.util.StringNormalizer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/public/catalog")
@Tag(name = "Multi-Domain Catalog & Autocomplete", description = "Public endpoints providing standardized subjects and skills across engineering domains (CS, Mechanical, ECE, EE, Chemical, Civil, AI/DS)")
public class CatalogController {

    private final SkillRepository skillRepository;

    public CatalogController(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @GetMapping("/domains")
    @Operation(summary = "Get all domain catalogs", description = "Returns all engineering branches with standard subjects and skills")
    public ResponseEntity<ApiResponse<List<CatalogData.DomainCatalog>>> getDomains() {
        return ResponseEntity.ok(ApiResponse.success(
                CatalogData.getAllDomains(),
                "Domain catalogs retrieved successfully"
        ));
    }

    @GetMapping("/domains/{domainCode}")
    @Operation(summary = "Get specific domain catalog", description = "Returns subject and skill catalog for a specific domain code (e.g. MECH, CSE, ECE, EE, CHEM, CIVIL, AI_DS)")
    public ResponseEntity<ApiResponse<CatalogData.DomainCatalog>> getDomainByCode(@PathVariable String domainCode) {
        Optional<CatalogData.DomainCatalog> domain = CatalogData.getDomainByCode(domainCode);
        return domain.map(d -> ResponseEntity.ok(ApiResponse.success(d, "Domain catalog retrieved")))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error(404, "Domain code '" + domainCode + "' not found")));
    }

    @GetMapping("/skills")
    @Operation(summary = "Autocomplete skills search", description = "Searches skills across standard curated domains and database with typo & prefix matching")
    public ResponseEntity<ApiResponse<List<String>>> searchSkills(
            @RequestParam(required = false) String query,
            @RequestParam(required = false, defaultValue = "30") int limit
    ) {
        Set<String> allSkills = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        allSkills.addAll(CatalogData.getAllStandardSkills());

        // Include any custom skills added to the platform
        for (Skill s : skillRepository.findAll()) {
            if (s.getName() != null && !s.getName().trim().isEmpty()) {
                allSkills.add(StringNormalizer.normalize(s.getName()));
            }
        }

        if (query == null || query.trim().isEmpty()) {
            List<String> list = allSkills.stream().limit(limit).toList();
            return ResponseEntity.ok(ApiResponse.success(list, "Skills retrieved"));
        }

        String normQuery = StringNormalizer.normalize(query).toLowerCase(Locale.ROOT);
        List<String> matched = allSkills.stream()
                .filter(s -> s.toLowerCase(Locale.ROOT).contains(normQuery) || StringNormalizer.isFuzzyMatch(s, normQuery))
                .sorted((a, b) -> {
                    boolean aStarts = a.toLowerCase(Locale.ROOT).startsWith(normQuery);
                    boolean bStarts = b.toLowerCase(Locale.ROOT).startsWith(normQuery);
                    if (aStarts && !bStarts) return -1;
                    if (!aStarts && bStarts) return 1;
                    return a.compareToIgnoreCase(b);
                })
                .limit(limit)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(matched, "Matched skills retrieved"));
    }

    @GetMapping("/subjects")
    @Operation(summary = "Autocomplete subjects search", description = "Searches standard subjects across all or specific engineering domains")
    public ResponseEntity<ApiResponse<List<String>>> searchSubjects(
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String query,
            @RequestParam(required = false, defaultValue = "30") int limit
    ) {
        Set<String> subjects = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        if (domain != null && !domain.trim().isEmpty()) {
            Optional<CatalogData.DomainCatalog> dc = CatalogData.getDomainByCode(domain.trim());
            dc.ifPresent(domainCatalog -> subjects.addAll(domainCatalog.subjects()));
        } else {
            subjects.addAll(CatalogData.getAllStandardSubjects());
        }

        if (query == null || query.trim().isEmpty()) {
            List<String> list = subjects.stream().limit(limit).toList();
            return ResponseEntity.ok(ApiResponse.success(list, "Subjects retrieved"));
        }

        String normQuery = StringNormalizer.normalize(query).toLowerCase(Locale.ROOT);
        List<String> matched = subjects.stream()
                .filter(s -> s.toLowerCase(Locale.ROOT).contains(normQuery) || StringNormalizer.isFuzzyMatch(s, normQuery))
                .sorted((a, b) -> {
                    boolean aStarts = a.toLowerCase(Locale.ROOT).startsWith(normQuery);
                    boolean bStarts = b.toLowerCase(Locale.ROOT).startsWith(normQuery);
                    if (aStarts && !bStarts) return -1;
                    if (!aStarts && bStarts) return 1;
                    return a.compareToIgnoreCase(b);
                })
                .limit(limit)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(matched, "Matched subjects retrieved"));
    }
}
