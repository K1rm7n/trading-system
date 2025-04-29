package com.tradingsystem.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for AlphaVantage API integration
 */
@Component
@ConfigurationProperties(prefix = "app.alpha-vantage")
public class AlphaVantageProperties {

    /**
     * API key for AlphaVantage
     */
    private String apiKey;

    /**
     * Base URL for AlphaVantage API
     * Default: https://www.alphavantage.co/query
     */
    private String baseUrl = "https://www.alphavantage.co/query";

    /**
     * Connection timeout in milliseconds
     * Default: 5000ms (5 seconds)
     */
    private int connectionTimeout = 5000;

    /**
     * Read timeout in milliseconds
     * Default: 5000ms (5 seconds)
     */
    private int readTimeout = 5000;

    /**
     * Maximum number of requests per minute
     * (AlphaVantage free tier limit is 5 requests per minute)
     * Default: 5
     */
    private int requestsPerMinute = 5;

    /**
     * Retry attempts for failed requests
     * Default: 3
     */
    private int retryAttempts = 3;

    /**
     * Retry delay in milliseconds
     * Default: 1000ms (1 second)
     */
    private int retryDelay = 1000;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public void setRequestsPerMinute(int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public int getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(int retryDelay) {
        this.retryDelay = retryDelay;
    }
}
