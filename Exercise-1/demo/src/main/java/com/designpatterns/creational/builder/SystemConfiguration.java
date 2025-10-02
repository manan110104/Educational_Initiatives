package com.designpatterns.creational.builder;

import com.designpatterns.core.ApplicationException;
import com.designpatterns.core.ValidationUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Complex system configuration class demonstrating the Builder Pattern.
 * This represents a sophisticated enterprise application configuration
 * with multiple subsystems, validation rules, and interdependencies.
 */
public final class SystemConfiguration {
    
    // Database Configuration
    private final DatabaseConfig databaseConfig;
    
    // Security Configuration
    private final SecurityConfig securityConfig;
    
    // Performance Configuration
    private final PerformanceConfig performanceConfig;
    
    // Monitoring Configuration
    private final MonitoringConfig monitoringConfig;
    
    // Feature Flags
    private final Map<String, Boolean> featureFlags;
    
    // Environment Settings
    private final String environment;
    private final String applicationName;
    private final String version;
    
    // Advanced Settings
    private final Map<String, Object> customProperties;
    private final List<String> enabledModules;
    private final Duration configurationTimeout;

    private SystemConfiguration(Builder builder) throws ApplicationException {
        // Validate required configurations
        this.databaseConfig = ValidationUtils.requireNonNull(builder.databaseConfig, "databaseConfig");
        this.securityConfig = ValidationUtils.requireNonNull(builder.securityConfig, "securityConfig");
        this.performanceConfig = ValidationUtils.requireNonNull(builder.performanceConfig, "performanceConfig");
        this.monitoringConfig = ValidationUtils.requireNonNull(builder.monitoringConfig, "monitoringConfig");
        
        // Validate basic settings
        this.environment = ValidationUtils.requireNonEmpty(builder.environment, "environment");
        this.applicationName = ValidationUtils.requireNonEmpty(builder.applicationName, "applicationName");
        this.version = ValidationUtils.requireNonEmpty(builder.version, "version");
        
        // Copy collections safely
        this.featureFlags = new ConcurrentHashMap<>(builder.featureFlags);
        this.customProperties = new ConcurrentHashMap<>(builder.customProperties);
        this.enabledModules = new ArrayList<>(builder.enabledModules);
        this.configurationTimeout = builder.configurationTimeout;
        
        // Perform cross-configuration validation
        validateConfiguration();
    }

    private void validateConfiguration() throws ApplicationException {
        // Validate environment-specific constraints
        if ("production".equalsIgnoreCase(environment)) {
            if (!securityConfig.isEncryptionEnabled()) {
                throw new ApplicationException(
                    "Encryption must be enabled in production environment",
                    "PRODUCTION_SECURITY_VIOLATION",
                    false
                );
            }
            
            if (performanceConfig.getMaxThreads() < 10) {
                throw new ApplicationException(
                    "Production environment requires at least 10 threads",
                    "PRODUCTION_PERFORMANCE_VIOLATION",
                    false
                );
            }
        }
        
        // Validate database and security compatibility
        if (databaseConfig.isClusterMode() && !securityConfig.isDistributedAuthEnabled()) {
            throw new ApplicationException(
                "Clustered database requires distributed authentication",
                "DATABASE_SECURITY_MISMATCH",
                false
            );
        }
        
        // Validate performance and monitoring alignment
        if (performanceConfig.isHighPerformanceMode() && !monitoringConfig.isDetailedMetricsEnabled()) {
            throw new ApplicationException(
                "High performance mode requires detailed metrics monitoring",
                "PERFORMANCE_MONITORING_MISMATCH",
                false
            );
        }
        
        // Validate module dependencies
        validateModuleDependencies();
    }

    private void validateModuleDependencies() throws ApplicationException {
        if (enabledModules.contains("analytics") && !enabledModules.contains("database")) {
            throw new ApplicationException(
                "Analytics module requires database module",
                "MODULE_DEPENDENCY_VIOLATION",
                false
            );
        }
        
        if (enabledModules.contains("reporting") && !enabledModules.contains("analytics")) {
            throw new ApplicationException(
                "Reporting module requires analytics module",
                "MODULE_DEPENDENCY_VIOLATION",
                false
            );
        }
    }

    // Getters
    public DatabaseConfig getDatabaseConfig() { return databaseConfig; }
    public SecurityConfig getSecurityConfig() { return securityConfig; }
    public PerformanceConfig getPerformanceConfig() { return performanceConfig; }
    public MonitoringConfig getMonitoringConfig() { return monitoringConfig; }
    public Map<String, Boolean> getFeatureFlags() { return new ConcurrentHashMap<>(featureFlags); }
    public String getEnvironment() { return environment; }
    public String getApplicationName() { return applicationName; }
    public String getVersion() { return version; }
    public Map<String, Object> getCustomProperties() { return new ConcurrentHashMap<>(customProperties); }
    public List<String> getEnabledModules() { return new ArrayList<>(enabledModules); }
    public Duration getConfigurationTimeout() { return configurationTimeout; }

    // Utility methods
    public boolean isFeatureEnabled(String featureName) {
        return featureFlags.getOrDefault(featureName, false);
    }
    
    public boolean isProductionEnvironment() {
        return "production".equalsIgnoreCase(environment);
    }
    
    public boolean isDevelopmentEnvironment() {
        return "development".equalsIgnoreCase(environment);
    }
    
    public boolean isModuleEnabled(String moduleName) {
        return enabledModules.contains(moduleName);
    }

    @Override
    public String toString() {
        return String.format("SystemConfiguration{env='%s', app='%s', version='%s', modules=%d, features=%d}",
                           environment, applicationName, version, enabledModules.size(), featureFlags.size());
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Sophisticated Builder class with fluent interface and validation.
     */
    public static class Builder {
        private DatabaseConfig databaseConfig;
        private SecurityConfig securityConfig;
        private PerformanceConfig performanceConfig;
        private MonitoringConfig monitoringConfig;
        private Map<String, Boolean> featureFlags = new ConcurrentHashMap<>();
        private String environment;
        private String applicationName;
        private String version;
        private Map<String, Object> customProperties = new ConcurrentHashMap<>();
        private List<String> enabledModules = new ArrayList<>();
        private Duration configurationTimeout = Duration.ofMinutes(5);

        // Database configuration
        public Builder database(DatabaseConfig databaseConfig) {
            this.databaseConfig = databaseConfig;
            return this;
        }

        public Builder database(String host, int port, String database, String username, String password) throws ApplicationException {
            this.databaseConfig = DatabaseConfig.builder()
                .host(host)
                .port(port)
                .database(database)
                .username(username)
                .password(password)
                .build();
            return this;
        }

        // Security configuration
        public Builder security(SecurityConfig securityConfig) {
            this.securityConfig = securityConfig;
            return this;
        }

        public Builder security(boolean encryptionEnabled, String encryptionAlgorithm, int sessionTimeoutMinutes) throws ApplicationException {
            this.securityConfig = SecurityConfig.builder()
                .encryptionEnabled(encryptionEnabled)
                .encryptionAlgorithm(encryptionAlgorithm)
                .sessionTimeoutMinutes(sessionTimeoutMinutes)
                .build();
            return this;
        }

        // Performance configuration
        public Builder performance(PerformanceConfig performanceConfig) {
            this.performanceConfig = performanceConfig;
            return this;
        }

        public Builder performance(int maxThreads, int connectionPoolSize, boolean cachingEnabled) throws ApplicationException {
            this.performanceConfig = PerformanceConfig.builder()
                .maxThreads(maxThreads)
                .connectionPoolSize(connectionPoolSize)
                .cachingEnabled(cachingEnabled)
                .build();
            return this;
        }

        // Monitoring configuration
        public Builder monitoring(MonitoringConfig monitoringConfig) {
            this.monitoringConfig = monitoringConfig;
            return this;
        }

        public Builder monitoring(boolean metricsEnabled, int metricsIntervalSeconds, boolean alertingEnabled) throws ApplicationException {
            this.monitoringConfig = MonitoringConfig.builder()
                .metricsEnabled(metricsEnabled)
                .metricsIntervalSeconds(metricsIntervalSeconds)
                .alertingEnabled(alertingEnabled)
                .build();
            return this;
        }

        // Environment settings
        public Builder environment(String environment) {
            this.environment = environment;
            return this;
        }

        public Builder applicationName(String applicationName) {
            this.applicationName = applicationName;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        // Feature flags
        public Builder enableFeature(String featureName) {
            this.featureFlags.put(featureName, true);
            return this;
        }

        public Builder disableFeature(String featureName) {
            this.featureFlags.put(featureName, false);
            return this;
        }

        public Builder feature(String featureName, boolean enabled) {
            this.featureFlags.put(featureName, enabled);
            return this;
        }

        public Builder features(Map<String, Boolean> features) {
            this.featureFlags.putAll(features);
            return this;
        }

        // Modules
        public Builder enableModule(String moduleName) {
            if (!this.enabledModules.contains(moduleName)) {
                this.enabledModules.add(moduleName);
            }
            return this;
        }

        public Builder enableModules(String... moduleNames) {
            for (String moduleName : moduleNames) {
                enableModule(moduleName);
            }
            return this;
        }

        public Builder enableModules(List<String> moduleNames) {
            for (String moduleName : moduleNames) {
                enableModule(moduleName);
            }
            return this;
        }

        // Custom properties
        public Builder property(String key, Object value) {
            this.customProperties.put(key, value);
            return this;
        }

        public Builder properties(Map<String, Object> properties) {
            this.customProperties.putAll(properties);
            return this;
        }

        // Timeout
        public Builder configurationTimeout(Duration timeout) {
            this.configurationTimeout = timeout;
            return this;
        }

        public Builder configurationTimeoutMinutes(int minutes) {
            this.configurationTimeout = Duration.ofMinutes(minutes);
            return this;
        }

        // Preset configurations for common environments
        public Builder developmentPreset() throws ApplicationException {
            return environment("development")
                .database("localhost", 5432, "dev_db", "dev_user", "dev_pass")
                .security(false, "AES", 60)
                .performance(5, 10, true)
                .monitoring(true, 30, false)
                .enableModules("database", "web", "api")
                .enableFeature("debug_mode")
                .enableFeature("hot_reload")
                .disableFeature("analytics");
        }

        public Builder productionPreset() throws ApplicationException {
            return environment("production")
                .security(true, "AES-256", 30)
                .performance(50, 100, true)
                .monitoring(true, 10, true)
                .enableModules("database", "web", "api", "analytics", "reporting")
                .enableFeature("analytics")
                .enableFeature("caching")
                .disableFeature("debug_mode");
        }

        public Builder testingPreset() throws ApplicationException {
            return environment("testing")
                .database("testdb", 5432, "test_db", "test_user", "test_pass")
                .security(false, "AES", 120)
                .performance(10, 20, false)
                .monitoring(true, 60, false)
                .enableModules("database", "web", "api")
                .enableFeature("test_mode")
                .disableFeature("analytics");
        }

        // Validation before building
        public Builder validate() throws ApplicationException {
            if (environment == null) {
                throw new ApplicationException("Environment must be specified", "MISSING_ENVIRONMENT", false);
            }
            
            if (applicationName == null) {
                throw new ApplicationException("Application name must be specified", "MISSING_APP_NAME", false);
            }
            
            if (version == null) {
                throw new ApplicationException("Version must be specified", "MISSING_VERSION", false);
            }
            
            return this;
        }

        // Build method
        public SystemConfiguration build() throws ApplicationException {
            // Auto-validate before building
            validate();
            
            // Set defaults if not specified
            if (databaseConfig == null) {
                databaseConfig = DatabaseConfig.defaultConfig();
            }
            
            if (securityConfig == null) {
                securityConfig = SecurityConfig.defaultConfig();
            }
            
            if (performanceConfig == null) {
                performanceConfig = PerformanceConfig.defaultConfig();
            }
            
            if (monitoringConfig == null) {
                monitoringConfig = MonitoringConfig.defaultConfig();
            }
            
            return new SystemConfiguration(this);
        }
    }
}
