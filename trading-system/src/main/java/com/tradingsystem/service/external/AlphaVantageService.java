package com.tradingsystem.service.external;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.tradingsystem.exception.ExternalServiceException;
import com.tradingsystem.model.dto.StockData;
import com.tradingsystem.model.dto.TechnicalIndicator;
import com.tradingsystem.properties.AlphaVantageProperties;

/**
 * Сервис для взаимодействия с Alpha Vantage API
 * Документация API: https://www.alphavantage.co/documentation/
 */
@Service
public class AlphaVantageService {
    private static final Logger logger = LoggerFactory.getLogger(AlphaVantageService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RestTemplate restTemplate;
    private final AlphaVantageProperties properties;
    private long lastApiCallTime = 0;

    @Autowired
    public AlphaVantageService(RestTemplate restTemplate, AlphaVantageProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    /**
     * Получает текущие данные по акции
     * @param symbol Тикер акции
     * @return Объект с данными акции
     */
    public StockData getQuote(String symbol) {
        try {
            logger.debug("Getting quote for symbol: {}", symbol);
            String url = buildUrl("GLOBAL_QUOTE", symbol);
            Map<String, Object> response = executeApiCall(url);

            if (response == null || !response.containsKey("Global Quote")) {
                throw new ExternalServiceException("Failed to get quote data for " + symbol);
            }

            @SuppressWarnings("unchecked")
            Map<String, String> quoteData = (Map<String, String>) response.get("Global Quote");
            return parseQuoteData(quoteData);
        } catch (RestClientException e) {
            logger.error("Error fetching quote for {}: {}", symbol, e.getMessage());
            throw new ExternalServiceException("Error fetching quote for " + symbol, e);
        }
    }

    /**
     * Получает внутридневные данные по акции
     * @param symbol Тикер акции
     * @param interval Интервал (1min, 5min, 15min, 30min, 60min)
     * @return Объект с историческими данными
     */
    public Map<String, Object> getIntradayData(String symbol, String interval) {
        try {
            logger.debug("Getting intraday data for symbol: {} with interval: {}", symbol, interval);
            String url = buildUrl("TIME_SERIES_INTRADAY", symbol, Map.of("interval", interval));
            return executeApiCall(url);
        } catch (RestClientException e) {
            logger.error("Error fetching intraday data for {}: {}", symbol, e.getMessage());
            throw new ExternalServiceException("Error fetching intraday data for " + symbol, e);
        }
    }

    /**
     * Получает дневные данные по акции
     * @param symbol Тикер акции
     * @return Объект с историческими данными
     */
    public Map<String, Object> getDailyData(String symbol) {
        try {
            logger.debug("Getting daily data for symbol: {}", symbol);
            String url = buildUrl("TIME_SERIES_DAILY", symbol);
            return executeApiCall(url);
        } catch (RestClientException e) {
            logger.error("Error fetching daily data for {}: {}", symbol, e.getMessage());
            throw new ExternalServiceException("Error fetching daily data for " + symbol, e);
        }
    }

    /**
     * Получает данные индикатора RSI
     * @param symbol Тикер акции
     * @param interval Интервал
     * @param timePeriod Период (обычно 14)
     * @return Объект с данными индикатора
     */
    public TechnicalIndicator getRSI(String symbol, String interval, int timePeriod) {
        try {
            logger.debug("Getting RSI for symbol: {} with interval: {} and period: {}", symbol, interval, timePeriod);
            Map<String, String> params = Map.of(
                    "interval", interval,
                    "time_period", String.valueOf(timePeriod),
                    "series_type", "close"
            );

            String url = buildUrl("RSI", symbol, params);
            Map<String, Object> response = executeApiCall(url);

            if (response == null || !response.containsKey("Technical Analysis: RSI")) {
                throw new ExternalServiceException("Failed to get RSI data for " + symbol);
            }

            @SuppressWarnings("unchecked")
            Map<String, Map<String, String>> rsiData =
                    (Map<String, Map<String, String>>) response.get("Technical Analysis: RSI");
            return parseRSIData(rsiData);
        } catch (RestClientException e) {
            logger.error("Error fetching RSI for {}: {}", symbol, e.getMessage());
            throw new ExternalServiceException("Error fetching RSI for " + symbol, e);
        }
    }

    /**
     * Получает данные индикатора MACD
     * @param symbol Тикер акции
     * @param interval Интервал
     * @param fastPeriod Быстрый период (обычно 12)
     * @param slowPeriod Медленный период (обычно 26)
     * @param signalPeriod Сигнальный период (обычно 9)
     * @return Объект с данными индикатора
     */
    public TechnicalIndicator getMACD(String symbol, String interval, int fastPeriod, int slowPeriod, int signalPeriod) {
        try {
            logger.debug("Getting MACD for symbol: {} with interval: {}", symbol, interval);
            Map<String, String> params = Map.of(
                    "interval", interval,
                    "series_type", "close",
                    "fastperiod", String.valueOf(fastPeriod),
                    "slowperiod", String.valueOf(slowPeriod),
                    "signalperiod", String.valueOf(signalPeriod)
            );

            String url = buildUrl("MACD", symbol, params);
            Map<String, Object> response = executeApiCall(url);

            if (response == null || !response.containsKey("Technical Analysis: MACD")) {
                throw new ExternalServiceException("Failed to get MACD data for " + symbol);
            }

            @SuppressWarnings("unchecked")
            Map<String, Map<String, String>> macdData =
                    (Map<String, Map<String, String>>) response.get("Technical Analysis: MACD");
            return parseMACDData(macdData);
        } catch (RestClientException e) {
            logger.error("Error fetching MACD for {}: {}", symbol, e.getMessage());
            throw new ExternalServiceException("Error fetching MACD for " + symbol, e);
        }
    }

    /**
     * Получает данные об объеме торгов
     * @param symbol Тикер акции
     * @return Объект с данными об объеме
     */
    public Map<String, Object> getVolumeData(String symbol) {
        try {
            logger.debug("Getting volume data for symbol: {}", symbol);
            String url = buildUrl("TIME_SERIES_DAILY", symbol);
            return executeApiCall(url);
        } catch (RestClientException e) {
            logger.error("Error fetching volume data for {}: {}", symbol, e.getMessage());
            throw new ExternalServiceException("Error fetching volume data for " + symbol, e);
        }
    }

    /**
     * Получает информацию о компании
     * @param symbol Тикер акции
     * @return Объект с информацией о компании
     */
    public Map<String, Object> getCompanyOverview(String symbol) {
        try {
            logger.debug("Getting company overview for symbol: {}", symbol);
            String url = buildUrl("OVERVIEW", symbol);
            return executeApiCall(url);
        } catch (RestClientException e) {
            logger.error("Error fetching company overview for {}: {}", symbol, e.getMessage());
            throw new ExternalServiceException("Error fetching company overview for " + symbol, e);
        }
    }

    /**
     * Выполняет поиск акций по ключевому слову
     * @param keywords Ключевые слова для поиска
     * @return Список найденных акций
     */
    public Map<String, Object> searchStocks(String keywords) {
        try {
            logger.debug("Searching stocks with keywords: {}", keywords);
            String url = buildUrl("SYMBOL_SEARCH", null, Map.of("keywords", keywords));
            return executeApiCall(url);
        } catch (RestClientException e) {
            logger.error("Error searching stocks for {}: {}", keywords, e.getMessage());
            throw new ExternalServiceException("Error searching stocks for " + keywords, e);
        }
    }

    // Вспомогательные методы

    /**
     * Создает URL для запроса к API
     * @param function Функция API
     * @param symbol Тикер акции
     * @return URL для запроса
     */
    private String buildUrl(String function, String symbol) {
        return buildUrl(function, symbol, Map.of());
    }

    /**
     * Создает URL для запроса к API с дополнительными параметрами
     * @param function Функция API
     * @param symbol Тикер акции
     * @param additionalParams Дополнительные параметры
     * @return URL для запроса
     */
    private String buildUrl(String function, String symbol, Map<String, String> additionalParams) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(properties.getUrl())
                .queryParam("function", function)
                .queryParam("apikey", properties.getApiKey());

        if (symbol != null) {
            builder.queryParam("symbol", symbol);
        }

        // Добавляем дополнительные параметры
        additionalParams.forEach(builder::queryParam);

        return builder.toUriString();
    }

    /**
     * Выполняет вызов API с соблюдением лимитов
     * @param url URL для запроса
     * @return Ответ от API
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> executeApiCall(String url) {
        // Соблюдаем лимиты API (обычно 5 запросов в минуту для бесплатного ключа)
        long currentTime = System.currentTimeMillis();
        long timeSinceLastCall = currentTime - lastApiCallTime;

        if (timeSinceLastCall < properties.getRequestDelay()) {
            try {
                TimeUnit.MILLISECONDS.sleep(properties.getRequestDelay() - timeSinceLastCall);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Выполняем запрос
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        lastApiCallTime = System.currentTimeMillis();

        // Проверяем на ошибки API
        if (response != null && response.containsKey("Error Message")) {
            String errorMessage = (String) response.get("Error Message");
            logger.error("Alpha Vantage API error: {}", errorMessage);
            throw new ExternalServiceException("Alpha Vantage API error: " + errorMessage);
        }

        if (response != null && response.containsKey("Note")) {
            String note = (String) response.get("Note");
            logger.warn("Alpha Vantage API note: {}", note);
            // Если превышен лимит, можно добавить более длительную задержку
            // или другую логику обработки
        }

        return response;
    }

    /**
     * Парсит данные котировки
     * @param quoteData Данные котировки из API
     * @return Объект StockData
     */
    private StockData parseQuoteData(Map<String, String> quoteData) {
        StockData stockData = new StockData();

        stockData.setSymbol(quoteData.get("01. symbol"));
        stockData.setCurrentPrice(new BigDecimal(quoteData.get("05. price")));
        stockData.setPreviousClose(new BigDecimal(quoteData.get("08. previous close")));
        stockData.setVolume(Long.parseLong(quoteData.get("06. volume")));
        stockData.setChangePercent(new BigDecimal(quoteData.get("10. change percent").replace("%", "")));

        // Дополнительные поля, если они есть в ответе API
        if (quoteData.containsKey("02. open")) {
            stockData.setOpen(new BigDecimal(quoteData.get("02. open")));
        }

        if (quoteData.containsKey("03. high")) {
            stockData.setHigh(new BigDecimal(quoteData.get("03. high")));
        }

        if (quoteData.containsKey("04. low")) {
            stockData.setLow(new BigDecimal(quoteData.get("04. low")));
        }

        stockData.setLastUpdated(LocalDateTime.now());

        return stockData;
    }

    /**
     * Парсит данные RSI
     * @param rsiData Данные RSI из API
     * @return Объект TechnicalIndicator
     */
    private TechnicalIndicator parseRSIData(Map<String, Map<String, String>> rsiData) {
        // Берем самую последнюю запись (по дате)
        String latestDate = rsiData.keySet().stream()
                .sorted((d1, d2) -> LocalDate.parse(d2, DATE_FORMATTER).compareTo(LocalDate.parse(d1, DATE_FORMATTER)))
                .findFirst()
                .orElseThrow(() -> new ExternalServiceException("No RSI data available"));

        Map<String, String> latestData = rsiData.get(latestDate);

        TechnicalIndicator indicator = new TechnicalIndicator();
        indicator.setName("RSI");
        indicator.setValue(new BigDecimal(latestData.get("RSI")));
        indicator.setDate(LocalDate.parse(latestDate, DATE_FORMATTER));

        return indicator;
    }

    /**
     * Парсит данные MACD
     * @param macdData Данные MACD из API
     * @return Объект TechnicalIndicator
     */
    private TechnicalIndicator parseMACDData(Map<String, Map<String, String>> macdData) {
        // Берем самую последнюю запись (по дате)
        String latestDate = macdData.keySet().stream()
                .sorted((d1, d2) -> LocalDate.parse(d2, DATE_FORMATTER).compareTo(LocalDate.parse(d1, DATE_FORMATTER)))
                .findFirst()
                .orElseThrow(() -> new ExternalServiceException("No MACD data available"));

        Map<String, String> latestData = macdData.get(latestDate);

        TechnicalIndicator indicator = new TechnicalIndicator();
        indicator.setName("MACD");
        indicator.setValue(new BigDecimal(latestData.get("MACD")));
        indicator.setSignal(new BigDecimal(latestData.get("MACD_Signal")));
        indicator.setHistogram(new BigDecimal(latestData.get("MACD_Hist")));
        indicator.setDate(LocalDate.parse(latestDate, DATE_FORMATTER));

        return indicator;
    }
}
