package com.tradingsystem.service.external;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradingsystem.exception.ExternalServiceException;
import com.tradingsystem.model.entity.Stock;
import com.tradingsystem.model.enums.TrendType;
import com.tradingsystem.properties.ChatGPTProperties;

/**
 * Сервис для взаимодействия с OpenAI ChatGPT API
 * для получения рекомендаций по торговым решениям
 */
@Service
public class ChatGPTService {
    private static final Logger logger = LoggerFactory.getLogger(ChatGPTService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ChatGPTProperties properties;

    @Autowired
    public ChatGPTService(RestTemplate restTemplate, ObjectMapper objectMapper, ChatGPTProperties properties) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    /**
     * Получает инвестиционную рекомендацию на основе данных об акции
     * @param stock Данные об акции
     * @param technicalData Технические индикаторы
     * @param trend Тип тренда
     * @return Текст рекомендации
     */
    public String getInvestmentAdvice(Stock stock, List<Map<String, Object>> technicalData, TrendType trend) {
        try {
            logger.debug("Getting investment advice for stock: {}", stock.getSymbol());

            HttpHeaders headers = createHeaders();
            Map<String, Object> requestBody = createRequestBody(stock, technicalData, trend);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    properties.getUrl(),
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            return extractAdviceFromResponse(response.getBody());
        } catch (RestClientException e) {
            logger.error("Error calling ChatGPT API: {}", e.getMessage());
            throw new ExternalServiceException("Error calling ChatGPT API", e);
        } catch (Exception e) {
            logger.error("Unexpected error when getting investment advice: {}", e.getMessage());
            throw new ExternalServiceException("Failed to get investment advice", e);
        }
    }

    /**
     * Создает заголовки для запроса к API
     * @return HttpHeaders
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + properties.getApiKey());
        return headers;
    }

    /**
     * Создает тело запроса к API
     * @param stock Данные об акции
     * @param technicalData Технические индикаторы
     * @param trend Тип тренда
     * @return Map с параметрами запроса
     */
    private Map<String, Object> createRequestBody(Stock stock, List<Map<String, Object>> technicalData, TrendType trend) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", properties.getModel());
        requestBody.put("temperature", properties.getTemperature());
        requestBody.put("max_tokens", properties.getMaxTokens());

        List<Map<String, String>> messages = new ArrayList<>();

        // Системное сообщение с инструкциями для модели
        messages.add(Map.of(
                "role", "system",
                "content", "You are a professional financial advisor specialized in stock market analysis. " +
                        "Provide concise, actionable advice based on the provided data. " +
                        "Your response should include a clear BUY, SELL, or HOLD recommendation, " +
                        "along with a brief rationale that a trader can understand quickly."
        ));

        // Пользовательское сообщение с данными для анализа
        String userMessage = buildPrompt(stock, technicalData, trend);
        messages.add(Map.of("role", "user", "content", userMessage));

        requestBody.put("messages", messages);

        return requestBody;
    }

    /**
     * Создает промпт для ChatGPT на основе данных об акции
     * @param stock Данные об акции
     * @param technicalData Технические индикаторы
     * @param trend Тип тренда
     * @return Текст промпта
     */
    private String buildPrompt(Stock stock, List<Map<String, Object>> technicalData, TrendType trend) {
        StringBuilder prompt = new StringBuilder();

        // Основная информация об акции
        prompt.append(String.format("Please analyze %s (%s) which is currently showing a %s trend. ",
                stock.getName(), stock.getSymbol(), trend.name().toLowerCase()));

        // Ценовая информация
        prompt.append("Current price: $").append(stock.getCurrentPrice());
        prompt.append(", Previous close: $").append(stock.getPreviousClose());
        prompt.append(", Day change: ").append(stock.getDayChangePercent()).append("%.\n\n");

        // Технические индикаторы
        prompt.append("Technical indicators:\n");
        for (Map<String, Object> indicator : technicalData) {
            prompt.append("- ").append(indicator.get("name")).append(": ").append(indicator.get("value"));

            // Добавляем дополнительные значения для MACD
            if ("MACD".equals(indicator.get("name")) && indicator.containsKey("signal")) {
                prompt.append(", Signal: ").append(indicator.get("signal"));
                prompt.append(", Histogram: ").append(indicator.get("histogram"));
            }

            prompt.append("\n");
        }

        // Запрос конкретной рекомендации
        prompt.append("\nBased on this information, please provide:\n");
        prompt.append("1. A brief analysis of the current situation (2-3 sentences)\n");
        prompt.append("2. A clear recommendation: BUY, SELL, or HOLD\n");
        prompt.append("3. A brief rationale for your recommendation (2-3 sentences)\n");
        prompt.append("4. One key risk factor to consider\n");

        return prompt.toString();
    }

    /**
     * Извлекает текст рекомендации из ответа API
     * @param responseBody Тело ответа от API
     * @return Текст рекомендации
     */
    @SuppressWarnings("unchecked")
    private String extractAdviceFromResponse(Map responseBody) {
        if (responseBody == null || !responseBody.containsKey("choices")) {
            throw new ExternalServiceException("Invalid response from ChatGPT API");
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");

        if (choices.isEmpty()) {
            throw new ExternalServiceException("No choices in ChatGPT API response");
        }

        Map<String, Object> choice = choices.get(0);

        if (!choice.containsKey("message")) {
            throw new ExternalServiceException("No message in ChatGPT API response");
        }

        Map<String, Object> message = (Map<String, Object>) choice.get("message");

        if (!message.containsKey("content")) {
            throw new ExternalServiceException("No content in ChatGPT API response message");
        }

        return (String) message.get("content");
    }

    /**
     * Анализирует текст рекомендации для извлечения ключевой рекомендации (BUY, SELL, HOLD)
     * @param adviceText Полный текст рекомендации
     * @return Ключевая рекомендация
     */
    public String extractRecommendation(String adviceText) {
        if (adviceText == null) {
            return "HOLD"; // По умолчанию
        }

        String upperText = adviceText.toUpperCase();

        if (upperText.contains("BUY")) {
            return "BUY";
        } else if (upperText.contains("SELL")) {
            return "SELL";
        } else {
            return "HOLD";
        }
    }

    /**
     * Вычисляет уровень уверенности на основе текста рекомендации
     * @param adviceText Полный текст рекомендации
     * @return Уровень уверенности от 0.0 до 1.0
     */
    public double calculateConfidence(String adviceText) {
        if (adviceText == null) {
            return 0.5; // Средний уровень уверенности по умолчанию
        }

        String lowerText = adviceText.toLowerCase();
        double confidence = 0.5; // Базовый уровень уверенности

        // Повышаем уверенность в зависимости от использованных слов
        if (lowerText.contains("strongly") || lowerText.contains("definitely") ||
                lowerText.contains("certainly") || lowerText.contains("highly")) {
            confidence += 0.3;
        } else if (lowerText.contains("recommend") || lowerText.contains("suggest") ||
                lowerText.contains("advise")) {
            confidence += 0.2;
        } else if (lowerText.contains("consider") || lowerText.contains("might") ||
                lowerText.contains("could")) {
            confidence += 0.1;
        }

        // Снижаем уверенность при наличии сомнений
        if (lowerText.contains("uncertain") || lowerText.contains("unclear") ||
                lowerText.contains("risky") || lowerText.contains("doubt")) {
            confidence -= 0.2;
        } else if (lowerText.contains("caution") || lowerText.contains("careful") ||
                lowerText.contains("wait")) {
            confidence -= 0.1;
        }

        // Ограничиваем значение в диапазоне [0.1, 0.95]
        return Math.max(0.1, Math.min(0.95, confidence));
    }
}
