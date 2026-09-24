package com.matcher.platform.security.mail;

public enum CircuitState {
    CLOSED,    // Healthy: requests permitted
    OPEN,      // Tripped / Degraded: requests skipped immediately without network lag
    HALF_OPEN  // Probing recovery: allows a trial request
}
