package com.tradingsystem.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for ChatGPT API integration
 */
@Component
@ConfigurationProperties(prefix = "app.chatgpt")
public class ChatGPTProperties {

    /**
     * API key for ChatGPT/OpenAI
     */
    private String apiKey;

    /**
     * Base URL for ChatGPT API
     * Default: https://api.openai.com/v1
     */
    private String baseUrl = "https://api.openai.com/v1";

    /**
     * Model to use for ChatGPT API requests
     * Default: gpt-4
     */
    private String model = "gpt-4";

    /**
     * Maximum tokens to generate in a response
     * Default: 1000
     */
    private int maxTokens = 1000;

    /**
     * Temperature parameter for response generation (0.0 to 1.0)
     * Lower values make responses more deterministic, higher values make them more random
     * Default: 0.5
     */
    private double temperature = 0.5;

    /**
     * Connection timeout in milliseconds
     * Default: 10000ms (10 seconds)
     */
    private int connectionTimeout = 10000;

    /**
     * Read timeout in milliseconds
     * Default: 30000ms (30 seconds)
     */
    private int readTimeout = 30000;

    /**
     * Retry attempts for failed requests
     * Default: 2
     */
    private int retryAttempts = 2;

    /**
     * System prompt to guide the model behavior for investment analysis
     */
    private String systemPrompt = "You are an AI assistant specialized in financial and investment analysis. " +
            "Provide objective, data-driven insights for investment decisions. " +
            "Analyze market trends, stock performance, and portfolio allocation. " +
            "Offer clear explanations suitable for both beginner and advanced investors.";

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

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
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

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }
}
