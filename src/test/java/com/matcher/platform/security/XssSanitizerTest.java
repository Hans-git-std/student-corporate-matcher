package com.matcher.platform.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XssSanitizerTest {

    @Test
    @DisplayName("Should neutralize script tags and javascript pseudo-protocols")
    void testSanitizeMaliciousInput() {
        String dirtyInput = "<script>alert('XSS')</script>Hello World <img src='x' onerror='alert(1)'>";
        String clean = XssSanitizer.sanitize(dirtyInput);

        assertThat(clean).doesNotContain("<script>");
        assertThat(clean).doesNotContain("</script>");
        assertThat(clean).doesNotContain("onerror=");
        assertThat(clean).contains("Hello World");
    }

    @Test
    @DisplayName("Should preserve normal text safely")
    void testPreserveNormalText() {
        String normalText = "Passionate backend developer with 2+ years of experience.";
        String clean = XssSanitizer.sanitize(normalText);

        assertThat(clean).isEqualTo(normalText);
    }
}
