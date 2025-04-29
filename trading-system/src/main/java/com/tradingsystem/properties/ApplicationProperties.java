package com.tradingsystem.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Main application configuration properties
 */
@Component
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

    /**
     * Application name
     */
    private String name = "Intelligent Investment Decision Support System";

    /**
     * Application version
     */
    private String version = "1.0.0";

    /**
     * Flag to enable/disable data caching
     */
    private boolean cacheEnabled = true;

    /**
     * Default cache expiration time in seconds
     */
    private int cacheExpiration = 3600;

    /**
     * Flag to enable/disable scheduled data updates
     */
    private boolean scheduledUpdatesEnabled = true;

    /**
     * Default data refresh interval in minutes
     */
    private int dataRefreshInterval = 60;

    /**
     * Flag to enable/disable detailed API logging
     */
    private boolean detailedApiLogging = false;

    /**
     * Default pagination size for list endpoints
     */
    private int defaultPageSize = 20;

    /**
     * Maximum allowed pagination size
     */
    private int maxPageSize = 100;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public int getCacheExpiration() {
        return cacheExpiration;
    }

    public void setCacheExpiration(int cacheExpiration) {
        this.cacheExpiration = cacheExpiration;
    }

    public boolean isScheduledUpdatesEnabled() {
        return scheduledUpdatesEnabled;
    }

    public void setScheduledUpdatesEnabled(boolean scheduledUpdatesEnabled) {
        this.scheduledUpdatesEnabled = scheduledUpdatesEnabled;
    }

    public int getDataRefreshInterval() {
        return dataRefreshInterval;
    }

    public void setDataRefreshInterval(int dataRefreshInterval) {
        this.dataRefreshInterval = dataRefreshInterval;
    }

    public boolean isDetailedApiLogging() {
        return detailedApiLogging;
    }

    public void setDetailedApiLogging(boolean detailedApiLogging) {
        this.detailedApiLogging = detailedApiLogging;
    }

    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    public void setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }

    public int getMaxPageSize() {
        return maxPageSize;
    }

    public void setMaxPageSize(int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }
}
