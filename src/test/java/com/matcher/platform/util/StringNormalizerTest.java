package com.matcher.platform.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringNormalizerTest {

    @Test
    @DisplayName("Should normalize multiple spaces and leading/trailing whitespaces")
    void testNormalize() {
        assertThat(StringNormalizer.normalize("  Data    Structures   &   Algorithms  "))
                .isEqualTo("Data Structures & Algorithms");
        assertThat(StringNormalizer.normalize("  Spring    Boot "))
                .isEqualTo("Spring Boot");
        assertThat(StringNormalizer.normalize(null)).isNull();
    }

    @Test
    @DisplayName("Should canonicalize strings by stripping punctuation, hyphens, dots, and converting ampersands")
    void testCanonicalize() {
        assertThat(StringNormalizer.canonicalize("Spring-Boot")).isEqualTo("springboot");
        assertThat(StringNormalizer.canonicalize("Spring Boot")).isEqualTo("springboot");
        assertThat(StringNormalizer.canonicalize("SpringBoot")).isEqualTo("springboot");
        assertThat(StringNormalizer.canonicalize("React.js")).isEqualTo("reactjs");
        assertThat(StringNormalizer.canonicalize("React JS")).isEqualTo("reactjs");
        assertThat(StringNormalizer.canonicalize("C++")).isEqualTo("c++");
        assertThat(StringNormalizer.canonicalize("C#")).isEqualTo("c#");
        assertThat(StringNormalizer.canonicalize("Data Structures & Algorithms")).isEqualTo("datastructuresandalgorithms");
        assertThat(StringNormalizer.canonicalize("Data Structures and Algorithms")).isEqualTo("datastructuresandalgorithms");
    }

    @Test
    @DisplayName("Should detect fuzzy match across spacing, canonical variations, and common typos")
    void testFuzzyMatch() {
        // Exact ignore-case & spacing
        assertThat(StringNormalizer.isFuzzyMatch("Spring Boot", "  spring   boot  ")).isTrue();

        // Canonical variations
        assertThat(StringNormalizer.isFuzzyMatch("Spring-Boot", "SpringBoot")).isTrue();
        assertThat(StringNormalizer.isFuzzyMatch("React.js", "React JS")).isTrue();
        assertThat(StringNormalizer.isFuzzyMatch("Data Structures & Algorithms", "Data Structures and Algorithms")).isTrue();

        // Typos with high similarity
        assertThat(StringNormalizer.isFuzzyMatch("Spirng Boot", "Spring Boot")).isTrue();
        assertThat(StringNormalizer.isFuzzyMatch("Thermodinamics", "Thermodynamics")).isTrue();
        assertThat(StringNormalizer.isFuzzyMatch("Kafak", "Kafka")).isTrue();
        assertThat(StringNormalizer.isFuzzyMatch("IC Engin", "IC Engine")).isTrue();
        assertThat(StringNormalizer.isFuzzyMatch("Data Structure", "Data Structures")).isTrue();

        // Distinct strings should NOT match
        assertThat(StringNormalizer.isFuzzyMatch("Java", "JavaScript")).isFalse();
        assertThat(StringNormalizer.isFuzzyMatch("Thermodynamics", "Fluid Mechanics")).isFalse();
        assertThat(StringNormalizer.isFuzzyMatch("C++", "Python")).isFalse();
    }
}
