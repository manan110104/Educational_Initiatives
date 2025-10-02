package com.designpatterns.creational.builder;

import com.designpatterns.core.ApplicationException;
import com.designpatterns.core.ValidationUtils;

import java.time.Duration;
import java.util.Objects;

/**
 * Database configuration class with its own builder pattern.
 * Demonstrates nested builders within the main Builder pattern.
 */
public final class DatabaseConfig {
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final int maxConnections;
    private final Duration connectionTimeout;
    private final boolean sslEnabled;
    private final boolean clusterMode;
    private final String driver;

    private DatabaseConfig(Builder builder) throws ApplicationException {
        this.host = ValidationUtils.requireNonEmpty(builder.host, "host");
        this.port = ValidationUtils.requireInRange(builder.port, 1, 65535, "port");
        this.database = ValidationUtils.requireNonEmpty(builder.database, "database");
        this.username = ValidationUtils.requireNonEmpty(builder.username, "username");
        this.password = ValidationUtils.requireNonEmpty(builder.password, "password");
        this.maxConnections = ValidationUtils.requireInRange(builder.maxConnections, 1, 1000, "maxConnections");
        this.connectionTimeout = ValidationUtils.requireNonNull(builder.connectionTimeout, "connectionTimeout");
        this.sslEnabled = builder.sslEnabled;
        this.clusterMode = builder.clusterMode;
        this.driver = ValidationUtils.requireNonEmpty(builder.driver, "driver");
    }

    // Getters
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getDatabase() { return database; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int getMaxConnections() { return maxConnections; }
    public Duration getConnectionTimeout() { return connectionTimeout; }
    public boolean isSslEnabled() { return sslEnabled; }
    public boolean isClusterMode() { return clusterMode; }
    public String getDriver() { return driver; }

    public String getConnectionUrl() {
        return String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatabaseConfig that = (DatabaseConfig) o;
        return port == that.port &&
               maxConnections == that.maxConnections &&
               sslEnabled == that.sslEnabled &&
               clusterMode == that.clusterMode &&
               Objects.equals(host, that.host) &&
               Objects.equals(database, that.database) &&
               Objects.equals(username, that.username) &&
               Objects.equals(connectionTimeout, that.connectionTimeout) &&
               Objects.equals(driver, that.driver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, database, username, maxConnections, connectionTimeout, sslEnabled, clusterMode, driver);
    }

    @Override
    public String toString() {
        return String.format("DatabaseConfig{host='%s', port=%d, database='%s', maxConnections=%d, ssl=%s, cluster=%s}",
                           host, port, database, maxConnections, sslEnabled, clusterMode);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static DatabaseConfig defaultConfig() throws ApplicationException {
        return builder()
            .host("localhost")
            .port(5432)
            .database("default_db")
            .username("default_user")
            .password("default_pass")
            .build();
    }

    public static class Builder {
        private String host = "localhost";
        private int port = 5432;
        private String database = "app_db";
        private String username = "app_user";
        private String password = "app_pass";
        private int maxConnections = 20;
        private Duration connectionTimeout = Duration.ofSeconds(30);
        private boolean sslEnabled = false;
        private boolean clusterMode = false;
        private String driver = "org.postgresql.Driver";

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder database(String database) {
            this.database = database;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder maxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        public Builder connectionTimeout(Duration connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder connectionTimeoutSeconds(int seconds) {
            this.connectionTimeout = Duration.ofSeconds(seconds);
            return this;
        }

        public Builder sslEnabled(boolean sslEnabled) {
            this.sslEnabled = sslEnabled;
            return this;
        }

        public Builder clusterMode(boolean clusterMode) {
            this.clusterMode = clusterMode;
            return this;
        }

        public Builder driver(String driver) {
            this.driver = driver;
            return this;
        }

        // Preset configurations
        public Builder postgresDefaults() {
            return driver("org.postgresql.Driver").port(5432);
        }

        public Builder mysqlDefaults() {
            return driver("com.mysql.cj.jdbc.Driver").port(3306);
        }

        public Builder productionDefaults() {
            return maxConnections(50)
                .connectionTimeoutSeconds(10)
                .sslEnabled(true)
                .clusterMode(true);
        }

        public DatabaseConfig build() throws ApplicationException {
            return new DatabaseConfig(this);
        }
    }
}
