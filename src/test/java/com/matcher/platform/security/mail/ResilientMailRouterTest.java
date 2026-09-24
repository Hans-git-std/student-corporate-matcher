package com.matcher.platform.security.mail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ResilientMailRouterTest {

    @Test
    @DisplayName("Should successfully route to Priority 1 provider when healthy")
    void testRouteToPrimaryProvider() throws Exception {
        MailProvider provider1 = mock(MailProvider.class);
        ProviderCircuitBreaker cb1 = new ProviderCircuitBreaker("Resend");
        when(provider1.getProviderName()).thenReturn("Resend");
        when(provider1.isConfigured()).thenReturn(true);
        when(provider1.getPriority()).thenReturn(1);
        when(provider1.getCircuitBreaker()).thenReturn(cb1);
        when(provider1.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);

        MailProvider provider2 = mock(MailProvider.class);
        ProviderCircuitBreaker cb2 = new ProviderCircuitBreaker("Brevo");
        when(provider2.getProviderName()).thenReturn("Brevo");
        when(provider2.isConfigured()).thenReturn(true);
        when(provider2.getPriority()).thenReturn(2);
        when(provider2.getCircuitBreaker()).thenReturn(cb2);

        ResilientMailRouter router = new ResilientMailRouter(List.of(provider2, provider1));

        boolean result = router.routeEmail("student@domain.com", "Subject", "<h1>OTP</h1>");

        assertThat(result).isTrue();
        verify(provider1, times(1)).sendEmail(eq("student@domain.com"), anyString(), anyString());
        verify(provider2, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should automatically failover to Priority 2 when Priority 1 throws or fails")
    void testFailoverToSecondaryProvider() throws Exception {
        MailProvider provider1 = mock(MailProvider.class);
        ProviderCircuitBreaker cb1 = new ProviderCircuitBreaker("Resend");
        when(provider1.getProviderName()).thenReturn("Resend");
        when(provider1.isConfigured()).thenReturn(true);
        when(provider1.getPriority()).thenReturn(1);
        when(provider1.getCircuitBreaker()).thenReturn(cb1);
        when(provider1.sendEmail(anyString(), anyString(), anyString())).thenReturn(false);

        MailProvider provider2 = mock(MailProvider.class);
        ProviderCircuitBreaker cb2 = new ProviderCircuitBreaker("Brevo");
        when(provider2.getProviderName()).thenReturn("Brevo");
        when(provider2.isConfigured()).thenReturn(true);
        when(provider2.getPriority()).thenReturn(2);
        when(provider2.getCircuitBreaker()).thenReturn(cb2);
        when(provider2.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);

        ResilientMailRouter router = new ResilientMailRouter(List.of(provider1, provider2));

        boolean result = router.routeEmail("student@domain.com", "Subject", "<h1>OTP</h1>");

        assertThat(result).isTrue();
        verify(provider1, times(1)).sendEmail(anyString(), anyString(), anyString());
        verify(provider2, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should immediately skip provider with OPEN circuit breaker without calling it")
    void testSkipOpenCircuit() throws Exception {
        MailProvider provider1 = mock(MailProvider.class);
        ProviderCircuitBreaker cb1 = new ProviderCircuitBreaker("Brevo");
        cb1.recordFailure("401 Unauthorized: IP restricted", true); // Tripped to OPEN

        when(provider1.getProviderName()).thenReturn("Brevo");
        when(provider1.isConfigured()).thenReturn(true);
        when(provider1.getPriority()).thenReturn(1);
        when(provider1.getCircuitBreaker()).thenReturn(cb1);

        MailProvider provider2 = mock(MailProvider.class);
        ProviderCircuitBreaker cb2 = new ProviderCircuitBreaker("SendGrid");
        when(provider2.getProviderName()).thenReturn("SendGrid");
        when(provider2.isConfigured()).thenReturn(true);
        when(provider2.getPriority()).thenReturn(2);
        when(provider2.getCircuitBreaker()).thenReturn(cb2);
        when(provider2.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);

        ResilientMailRouter router = new ResilientMailRouter(List.of(provider1, provider2));

        boolean result = router.routeEmail("student@domain.com", "Subject", "<h1>OTP</h1>");

        assertThat(result).isTrue();
        // Provider 1 must NEVER be called because its circuit is OPEN!
        verify(provider1, never()).sendEmail(anyString(), anyString(), anyString());
        verify(provider2, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should report correct health and telemetry for all providers")
    void testGetProvidersStatus() {
        MailProvider p1 = mock(MailProvider.class);
        ProviderCircuitBreaker cb1 = new ProviderCircuitBreaker("Resend");
        when(p1.getProviderName()).thenReturn("Resend");
        when(p1.isConfigured()).thenReturn(true);
        when(p1.getPriority()).thenReturn(1);
        when(p1.getCircuitBreaker()).thenReturn(cb1);

        ResilientMailRouter router = new ResilientMailRouter(List.of(p1));
        List<MailProviderStatusDto> statusList = router.getProvidersStatus();

        assertThat(statusList).hasSize(1);
        assertThat(statusList.get(0).providerName()).isEqualTo("Resend");
        assertThat(statusList.get(0).circuitState()).isEqualTo(CircuitState.CLOSED);
        assertThat(statusList.get(0).configured()).isTrue();
    }
}
