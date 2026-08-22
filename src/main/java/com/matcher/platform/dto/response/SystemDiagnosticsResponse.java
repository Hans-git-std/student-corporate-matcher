package com.matcher.platform.dto.response;

import java.util.Map;

public class SystemDiagnosticsResponse {

    private String serverStatus;
    private String jvmVersion;
    private long uptimeSeconds;
    private Map<String, Object> memoryUsage;
    private Map<String, Object> databaseStats;
    private Map<String, Object> mailQuotaStats;
    private String adminEmail;
    private String adminRecoveryEmail;

    public SystemDiagnosticsResponse() {
    }

    public SystemDiagnosticsResponse(String serverStatus, String jvmVersion, long uptimeSeconds, Map<String, Object> memoryUsage, Map<String, Object> databaseStats, Map<String, Object> mailQuotaStats, String adminEmail, String adminRecoveryEmail) {
        this.serverStatus = serverStatus;
        this.jvmVersion = jvmVersion;
        this.uptimeSeconds = uptimeSeconds;
        this.memoryUsage = memoryUsage;
        this.databaseStats = databaseStats;
        this.mailQuotaStats = mailQuotaStats;
        this.adminEmail = adminEmail;
        this.adminRecoveryEmail = adminRecoveryEmail;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String serverStatus;
        private String jvmVersion;
        private long uptimeSeconds;
        private Map<String, Object> memoryUsage;
        private Map<String, Object> databaseStats;
        private Map<String, Object> mailQuotaStats;
        private String adminEmail;
        private String adminRecoveryEmail;

        public Builder serverStatus(String serverStatus) {
            this.serverStatus = serverStatus;
            return this;
        }

        public Builder jvmVersion(String jvmVersion) {
            this.jvmVersion = jvmVersion;
            return this;
        }

        public Builder uptimeSeconds(long uptimeSeconds) {
            this.uptimeSeconds = uptimeSeconds;
            return this;
        }

        public Builder memoryUsage(Map<String, Object> memoryUsage) {
            this.memoryUsage = memoryUsage;
            return this;
        }

        public Builder databaseStats(Map<String, Object> databaseStats) {
            this.databaseStats = databaseStats;
            return this;
        }

        public Builder mailQuotaStats(Map<String, Object> mailQuotaStats) {
            this.mailQuotaStats = mailQuotaStats;
            return this;
        }

        public Builder adminEmail(String adminEmail) {
            this.adminEmail = adminEmail;
            return this;
        }

        public Builder adminRecoveryEmail(String adminRecoveryEmail) {
            this.adminRecoveryEmail = adminRecoveryEmail;
            return this;
        }

        public SystemDiagnosticsResponse build() {
            return new SystemDiagnosticsResponse(serverStatus, jvmVersion, uptimeSeconds, memoryUsage, databaseStats, mailQuotaStats, adminEmail, adminRecoveryEmail);
        }
    }

    public String getServerStatus() {
        return serverStatus;
    }

    public void setServerStatus(String serverStatus) {
        this.serverStatus = serverStatus;
    }

    public String getJvmVersion() {
        return jvmVersion;
    }

    public void setJvmVersion(String jvmVersion) {
        this.jvmVersion = jvmVersion;
    }

    public long getUptimeSeconds() {
        return uptimeSeconds;
    }

    public void setUptimeSeconds(long uptimeSeconds) {
        this.uptimeSeconds = uptimeSeconds;
    }

    public Map<String, Object> getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(Map<String, Object> memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public Map<String, Object> getDatabaseStats() {
        return databaseStats;
    }

    public void setDatabaseStats(Map<String, Object> databaseStats) {
        this.databaseStats = databaseStats;
    }

    public Map<String, Object> getMailQuotaStats() {
        return mailQuotaStats;
    }

    public void setMailQuotaStats(Map<String, Object> mailQuotaStats) {
        this.mailQuotaStats = mailQuotaStats;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getAdminRecoveryEmail() {
        return adminRecoveryEmail;
    }

    public void setAdminRecoveryEmail(String adminRecoveryEmail) {
        this.adminRecoveryEmail = adminRecoveryEmail;
    }
}
