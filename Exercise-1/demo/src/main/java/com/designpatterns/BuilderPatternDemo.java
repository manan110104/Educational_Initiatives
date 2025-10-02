package com.designpatterns;

import com.designpatterns.creational.builder.*;
import com.designpatterns.core.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstration class for the Builder Pattern implementation.
 */
public class BuilderPatternDemo {
    private static final Logger logger = LoggerFactory.getLogger(BuilderPatternDemo.class);

    public void demonstrate() throws ApplicationException {
        System.out.println("🔨 Building system configurations with different presets...");
        
        System.out.println("\n🚀 Development Environment Configuration:");
        SystemConfiguration devConfig = SystemConfiguration.builder()
            .applicationName("DesignPatternsShowcase")
            .version("1.0.0-SNAPSHOT")
            .developmentPreset()
            .enableFeature("hot_reload")
            .enableFeature("debug_logging")
            .property("dev.port", "8080")
            .property("dev.auto_restart", "true")
            .build();
        
        displayConfiguration(devConfig);
        
        System.out.println("\n🏭 Production Environment Configuration:");
        SystemConfiguration prodConfig = SystemConfiguration.builder()
            .applicationName("DesignPatternsShowcase")
            .version("1.0.0")
            .productionPreset()
            .database("prod-db.company.com", 5432, "prod_app_db", "prod_user", "secure_password")
            .enableFeature("monitoring")
            .enableFeature("alerting")
            .property("prod.cluster_size", "5")
            .property("prod.backup_enabled", "true")
            .configurationTimeoutMinutes(10)
            .build();
        
        displayConfiguration(prodConfig);
        
        System.out.println("\n🧪 Testing Environment Configuration:");
        SystemConfiguration testConfig = SystemConfiguration.builder()
            .applicationName("DesignPatternsShowcase")
            .version("1.0.0-TEST")
            .testingPreset()
            .enableFeature("test_data_generation")
            .enableFeature("mock_external_services")
            .property("test.parallel_execution", "true")
            .property("test.cleanup_after_run", "true")
            .build();
        
        displayConfiguration(testConfig);
        
        System.out.println("\n🔧 Custom High-Performance Configuration:");
        SystemConfiguration customConfig = SystemConfiguration.builder()
            .applicationName("DesignPatternsShowcase")
            .version("1.0.0-CUSTOM")
            .environment("staging")
            .database(DatabaseConfig.builder()
                .host("staging-db.company.com")
                .port(5432)
                .database("staging_db")
                .username("staging_user")
                .password("staging_pass")
                .productionDefaults()
                .build())
            .security(SecurityConfig.builder()
                .highSecurityPreset()
                .allowedOrigin("https://staging.company.com")
                .build())
            .performance(PerformanceConfig.builder()
                .highPerformancePreset()
                .build())
            .monitoring(MonitoringConfig.builder()
                .productionMonitoringPreset()
                .build())
            .enableModules("database", "web", "api", "analytics", "caching")
            .enableFeature("advanced_caching")
            .enableFeature("load_balancing")
            .enableFeature("auto_scaling")
            .property("custom.optimization_level", "maximum")
            .build();
        
        displayConfiguration(customConfig);
        
        System.out.println("\n✨ Builder Pattern demonstration completed!");
    }
    
    private void displayConfiguration(SystemConfiguration config) {
        System.out.println("  📋 Configuration: " + config);
        System.out.println("  🌍 Environment: " + config.getEnvironment());
        System.out.println("  🗄️  Database: " + config.getDatabaseConfig());
        System.out.println("  🔒 Security: " + config.getSecurityConfig());
        System.out.println("  ⚡ Performance: " + config.getPerformanceConfig());
        System.out.println("  📊 Monitoring: " + config.getMonitoringConfig());
        System.out.println("  🔧 Enabled Modules: " + config.getEnabledModules());
        System.out.println("  🎛️  Feature Flags: " + config.getFeatureFlags().size() + " features");
        System.out.println("  ⏱️  Timeout: " + config.getConfigurationTimeout());
        
        // Show some feature flags
        config.getFeatureFlags().entrySet().stream()
            .limit(3)
            .forEach(entry -> System.out.println("    🎯 " + entry.getKey() + ": " + 
                                               (entry.getValue() ? "✅" : "❌")));
    }
}
