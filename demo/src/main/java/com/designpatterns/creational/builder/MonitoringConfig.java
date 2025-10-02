package com.designpatterns.creational.builder;

import com.designpatterns.core.ApplicationException;
import com.designpatterns.core.ValidationUtils;

import java.util.List;
import java.util.Objects;
import java.util.ArrayList;

/**
 * Monitoring configuration class with alerting and metrics settings.
 */
public final class MonitoringConfig {
    private final boolean metricsEnabled;
    private final int metricsIntervalSeconds;
    private final boolean alertingEnabled;
    private final boolean detailedMetricsEnabled;
    private final List<String> alertChannels;
    private final int alertThresholdPercent;
    private final boolean healthCheckEnabled;
    private final int healthCheckIntervalSeconds;
    private final String metricsEndpoint;

    private MonitoringConfig(Builder builder) throws ApplicationException {
        this.metricsEnabled = builder.metricsEnabled;
        this.metricsIntervalSeconds = ValidationUtils.requireInRange(builder.metricsIntervalSeconds, 1, 3600, "metricsIntervalSeconds");
        this.alertingEnabled = builder.alertingEnabled;
        this.detailedMetricsEnabled = builder.detailedMetricsEnabled;
        this.alertChannels = new ArrayList<>(ValidationUtils.requireNonNull(builder.alertChannels, "alertChannels"));
        this.alertThresholdPercent = ValidationUtils.requireInRange(builder.alertThresholdPercent, 1, 100, "alertThresholdPercent");
        this.healthCheckEnabled = builder.healthCheckEnabled;
        this.healthCheckIntervalSeconds = ValidationUtils.requireInRange(builder.healthCheckIntervalSeconds, 1, 3600, "healthCheckIntervalSeconds");
        this.metricsEndpoint = builder.metricsEndpoint;

        validateConfiguration();
    }

    private void validateConfiguration() throws ApplicationException {
        if (alertingEnabled && alertChannels.isEmpty()) {
            throw new ApplicationException(
                "Alerting is enabled but no alert channels are configured",
                "NO_ALERT_CHANNELS",
                false
            );
        }

        if (detailedMetricsEnabled && metricsIntervalSeconds > 60) {
            throw new ApplicationException(
                "Detailed metrics require interval <= 60 seconds",
                "DETAILED_METRICS_INTERVAL_VIOLATION",
                false
            );
        }
    }

    // Getters
    public boolean isMetricsEnabled() { return metricsEnabled; }
    public int getMetricsIntervalSeconds() { return metricsIntervalSeconds; }
    public boolean isAlertingEnabled() { return alertingEnabled; }
    public boolean isDetailedMetricsEnabled() { return detailedMetricsEnabled; }
    public List<String> getAlertChannels() { return new ArrayList<>(alertChannels); }
    public int getAlertThresholdPercent() { return alertThresholdPercent; }
    public boolean isHealthCheckEnabled() { return healthCheckEnabled; }
    public int getHealthCheckIntervalSeconds() { return healthCheckIntervalSeconds; }
    public String getMetricsEndpoint() { return metricsEndpoint; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MonitoringConfig that = (MonitoringConfig) o;
        return metricsEnabled == that.metricsEnabled &&
               metricsIntervalSeconds == that.metricsIntervalSeconds &&
               alertingEnabled == that.alertingEnabled &&
               detailedMetricsEnabled == that.detailedMetricsEnabled &&
               alertThresholdPercent == that.alertThresholdPercent &&
               healthCheckEnabled == that.healthCheckEnabled &&
               healthCheckIntervalSeconds == that.healthCheckIntervalSeconds &&
               Objects.equals(alertChannels, that.alertChannels) &&
               Objects.equals(metricsEndpoint, that.metricsEndpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metricsEnabled, metricsIntervalSeconds, alertingEnabled, detailedMetricsEnabled,
                          alertChannels, alertThresholdPercent, healthCheckEnabled, healthCheckIntervalSeconds, metricsEndpoint);
    }

    @Override
    public String toString() {
        return String.format("MonitoringConfig{metrics=%s, interval=%ds, alerting=%s, detailed=%s, channels=%d}",
                           metricsEnabled, metricsIntervalSeconds, alertingEnabled, detailedMetricsEnabled, alertChannels.size());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static MonitoringConfig defaultConfig() throws ApplicationException {
        return builder().build();
    }

    public static class Builder {
        private boolean metricsEnabled = true;
        private int metricsIntervalSeconds = 60;
        private boolean alertingEnabled = false;
        private boolean detailedMetricsEnabled = false;
        private List<String> alertChannels = new ArrayList<>();
        private int alertThresholdPercent = 80;
        private boolean healthCheckEnabled = true;
        private int healthCheckIntervalSeconds = 30;
        private String metricsEndpoint = "/metrics";

        public Builder metricsEnabled(boolean metricsEnabled) {
            this.metricsEnabled = metricsEnabled;
            return this;
        }

        public Builder metricsIntervalSeconds(int metricsIntervalSeconds) {
            this.metricsIntervalSeconds = metricsIntervalSeconds;
            return this;
        }

        public Builder alertingEnabled(boolean alertingEnabled) {
            this.alertingEnabled = alertingEnabled;
            return this;
        }

        public Builder detailedMetricsEnabled(boolean detailedMetricsEnabled) {
            this.detailedMetricsEnabled = detailedMetricsEnabled;
            return this;
        }

        public Builder alertChannel(String channel) {
            this.alertChannels.add(channel);
            return this;
        }

        public Builder alertChannels(List<String> channels) {
            this.alertChannels.addAll(channels);
            return this;
        }

        public Builder alertThresholdPercent(int alertThresholdPercent) {
            this.alertThresholdPercent = alertThresholdPercent;
            return this;
        }

        public Builder healthCheckEnabled(boolean healthCheckEnabled) {
            this.healthCheckEnabled = healthCheckEnabled;
            return this;
        }

        public Builder healthCheckIntervalSeconds(int healthCheckIntervalSeconds) {
            this.healthCheckIntervalSeconds = healthCheckIntervalSeconds;
            return this;
        }

        public Builder metricsEndpoint(String metricsEndpoint) {
            this.metricsEndpoint = metricsEndpoint;
            return this;
        }

        // Preset configurations
        public Builder productionMonitoringPreset() {
            return metricsEnabled(true)
                .metricsIntervalSeconds(30)
                .alertingEnabled(true)
                .detailedMetricsEnabled(true)
                .alertChannel("email")
                .alertChannel("slack")
                .alertThresholdPercent(85)
                .healthCheckEnabled(true)
                .healthCheckIntervalSeconds(15);
        }

        public Builder developmentMonitoringPreset() {
            return metricsEnabled(true)
                .metricsIntervalSeconds(120)
                .alertingEnabled(false)
                .detailedMetricsEnabled(false)
                .healthCheckEnabled(true)
                .healthCheckIntervalSeconds(60);
        }

        public MonitoringConfig build() throws ApplicationException {
            return new MonitoringConfig(this);
        }
    }
}
