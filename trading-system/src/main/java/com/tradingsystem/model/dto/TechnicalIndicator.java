package com.tradingsystem.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для хранения данных технических индикаторов
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalIndicator {

    /**
     * Название индикатора (например, RSI, MACD, SMA)
     */
    private String name;

    /**
     * Основное значение индикатора
     */
    private BigDecimal value;

    /**
     * Сигнальная линия (для индикаторов, имеющих сигнальную линию, например MACD)
     */
    private BigDecimal signal;

    /**
     * Гистограмма (для индикаторов с гистограммой, например MACD)
     */
    private BigDecimal histogram;

    /**
     * Период индикатора (например, 14 для RSI)
     */
    private Integer period;

    /**
     * Дата расчета индикатора
     */
    private LocalDate date;
}
