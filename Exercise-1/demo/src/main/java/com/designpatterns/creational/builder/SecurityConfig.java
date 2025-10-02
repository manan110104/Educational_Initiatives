package com.designpatterns.creational.builder;

import com.designpatterns.core.ApplicationException;
import com.designpatterns.core.ValidationUtils;

import java.util.List;
import java.util.Objects;
import java.util.ArrayList;

/**
 * Security configuration class demonstrating complex validation and interdependent settings.
 */
public final class SecurityConfig {
    private final boolean encryptionEnabled;
    private final String encryptionAlgorithm;
    private final int keySize;
    private final int sessionTimeoutMinutes;
    private final boolean distributedAuthEnabled;
    private final String authenticationProvider;
    private final List<String> allowedOrigins;
    private final boolean auditLoggingEnabled;
    private final int maxLoginAttempts;
    private final int lockoutDurationMinutes;

    private SecurityConfig(Builder builder) throws ApplicationException {
        this.encryptionEnabled = builder.encryptionEnabled;
        this.encryptionAlgorithm = ValidationUtils.requireNonEmpty(builder.encryptionAlgorithm, "encryptionAlgorithm");
        this.keySize = ValidationUtils.requireInRange(builder.keySize, 128, 512, "keySize");
        this.sessionTimeoutMinutes = ValidationUtils.requireInRange(builder.sessionTimeoutMinutes, 1, 1440, "sessionTimeoutMinutes");
        this.distributedAuthEnabled = builder.distributedAuthEnabled;
        this.authenticationProvider = ValidationUtils.requireNonEmpty(builder.authenticationProvider, "authenticationProvider");
        this.allowedOrigins = new ArrayList<>(ValidationUtils.requireNonNull(builder.allowedOrigins, "allowedOrigins"));
        this.auditLoggingEnabled = builder.auditLoggingEnabled;
        this.maxLoginAttempts = ValidationUtils.requireInRange(builder.maxLoginAttempts, 1, 20, "maxLoginAttempts");
        this.lockoutDurationMinutes = ValidationUtils.requireInRange(builder.lockoutDurationMinutes, 1, 1440, "lockoutDurationMinutes");

        validateConfiguration();
    }

    private void validateConfiguration() throws ApplicationException {
        if (encryptionEnabled) {
            if ("AES".equals(encryptionAlgorithm) && keySize != 128 && keySize != 192 && keySize != 256) {
                throw new ApplicationException(
                    "AES encryption requires key size of 128, 192, or 256 bits",
                    "INVALID_AES_KEY_SIZE",
                    false
                );
            }
        }

        if (distributedAuthEnabled && sessionTimeoutMinutes > 60) {
            throw new ApplicationException(
                "Distributed authentication requires session timeout <= 60 minutes",
                "DISTRIBUTED_AUTH_TIMEOUT_VIOLATION",
                false
            );
        }
    }

    // Getters
    public boolean isEncryptionEnabled() { return encryptionEnabled; }
    public String getEncryptionAlgorithm() { return encryptionAlgorithm; }
    public int getKeySize() { return keySize; }
    public int getSessionTimeoutMinutes() { return sessionTimeoutMinutes; }
    public boolean isDistributedAuthEnabled() { return distributedAuthEnabled; }
    public String getAuthenticationProvider() { return authenticationProvider; }
    public List<String> getAllowedOrigins() { return new ArrayList<>(allowedOrigins); }
    public boolean isAuditLoggingEnabled() { return auditLoggingEnabled; }
    public int getMaxLoginAttempts() { return maxLoginAttempts; }
    public int getLockoutDurationMinutes() { return lockoutDurationMinutes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecurityConfig that = (SecurityConfig) o;
        return encryptionEnabled == that.encryptionEnabled &&
               keySize == that.keySize &&
               sessionTimeoutMinutes == that.sessionTimeoutMinutes &&
               distributedAuthEnabled == that.distributedAuthEnabled &&
               auditLoggingEnabled == that.auditLoggingEnabled &&
               maxLoginAttempts == that.maxLoginAttempts &&
               lockoutDurationMinutes == that.lockoutDurationMinutes &&
               Objects.equals(encryptionAlgorithm, that.encryptionAlgorithm) &&
               Objects.equals(authenticationProvider, that.authenticationProvider) &&
               Objects.equals(allowedOrigins, that.allowedOrigins);
    }

    @Override
    public int hashCode() {
        return Objects.hash(encryptionEnabled, encryptionAlgorithm, keySize, sessionTimeoutMinutes,
                          distributedAuthEnabled, authenticationProvider, allowedOrigins, auditLoggingEnabled,
                          maxLoginAttempts, lockoutDurationMinutes);
    }

    @Override
    public String toString() {
        return String.format("SecurityConfig{encryption=%s, algorithm='%s', keySize=%d, sessionTimeout=%d, distributedAuth=%s}",
                           encryptionEnabled, encryptionAlgorithm, keySize, sessionTimeoutMinutes, distributedAuthEnabled);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static SecurityConfig defaultConfig() throws ApplicationException {
        return builder().build();
    }

    public static class Builder {
        private boolean encryptionEnabled = false;
        private String encryptionAlgorithm = "AES";
        private int keySize = 256;
        private int sessionTimeoutMinutes = 30;
        private boolean distributedAuthEnabled = false;
        private String authenticationProvider = "local";
        private List<String> allowedOrigins = new ArrayList<>(List.of("localhost"));
        private boolean auditLoggingEnabled = true;
        private int maxLoginAttempts = 5;
        private int lockoutDurationMinutes = 15;

        public Builder encryptionEnabled(boolean encryptionEnabled) {
            this.encryptionEnabled = encryptionEnabled;
            return this;
        }

        public Builder encryptionAlgorithm(String encryptionAlgorithm) {
            this.encryptionAlgorithm = encryptionAlgorithm;
            return this;
        }

        public Builder keySize(int keySize) {
            this.keySize = keySize;
            return this;
        }

        public Builder sessionTimeoutMinutes(int sessionTimeoutMinutes) {
            this.sessionTimeoutMinutes = sessionTimeoutMinutes;
            return this;
        }

        public Builder distributedAuthEnabled(boolean distributedAuthEnabled) {
            this.distributedAuthEnabled = distributedAuthEnabled;
            return this;
        }

        public Builder authenticationProvider(String authenticationProvider) {
            this.authenticationProvider = authenticationProvider;
            return this;
        }

        public Builder allowedOrigin(String origin) {
            this.allowedOrigins.add(origin);
            return this;
        }

        public Builder allowedOrigins(List<String> origins) {
            this.allowedOrigins.addAll(origins);
            return this;
        }

        public Builder auditLoggingEnabled(boolean auditLoggingEnabled) {
            this.auditLoggingEnabled = auditLoggingEnabled;
            return this;
        }

        public Builder maxLoginAttempts(int maxLoginAttempts) {
            this.maxLoginAttempts = maxLoginAttempts;
            return this;
        }

        public Builder lockoutDurationMinutes(int lockoutDurationMinutes) {
            this.lockoutDurationMinutes = lockoutDurationMinutes;
            return this;
        }

        // Preset configurations
        public Builder highSecurityPreset() {
            return encryptionEnabled(true)
                .encryptionAlgorithm("AES")
                .keySize(256)
                .sessionTimeoutMinutes(15)
                .distributedAuthEnabled(true)
                .auditLoggingEnabled(true)
                .maxLoginAttempts(3)
                .lockoutDurationMinutes(30);
        }

        public Builder developmentPreset() {
            return encryptionEnabled(false)
                .sessionTimeoutMinutes(120)
                .distributedAuthEnabled(false)
                .maxLoginAttempts(10)
                .lockoutDurationMinutes(5);
        }

        public SecurityConfig build() throws ApplicationException {
            return new SecurityConfig(this);
        }
    }
}
