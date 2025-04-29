package com.tradingsystem.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tradingsystem.properties.AlphaVantageProperties;
import com.tradingsystem.properties.ChatGPTProperties;
import com.tradingsystem.properties.JwtProperties;

@Configuration
public class ApplicationConfig {

    @Bean
    @ConfigurationProperties(prefix = "alphavantage")
    public AlphaVantageProperties alphaVantageProperties() {
        return new AlphaVantageProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "openai")
    public ChatGPTProperties chatGPTProperties() {
        return new ChatGPTProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "jwt")
    public JwtProperties jwtProperties() {
        return new JwtProperties();
    }
}
