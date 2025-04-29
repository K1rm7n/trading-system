package com.tradingsystem.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для хранения данных, полученных от AlphaVantage API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockData {

    /**
     * Символ акции
     */
    private String symbol;

    /**
     * Текущая цена
     */
    private BigDecimal currentPrice;

    /**
     * Цена закрытия предыдущего дня
     */
    private BigDecimal previousClose;

    /**
     * Объем торгов
     */
    private Long volume;

    /**
     * Процент изменения цены
     */
    private BigDecimal changePercent;

    /**
     * Наивысшая цена дня
     */
    private BigDecimal high;

    /**
     * Самая низкая цена дня
     */
    private BigDecimal low;

    /**
     * Цена открытия
     */
    private BigDecimal open;

    /**
     * Время последнего обновления данных
     */
    private LocalDateTime lastUpdated;

    /**
     * Исторические данные по таймфреймам (key - дата, value - значения)
     */
    private Map<String, Map<String, String>> timeSeries;
}