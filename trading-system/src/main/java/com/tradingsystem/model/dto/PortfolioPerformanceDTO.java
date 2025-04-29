package com.tradingsystem.model.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи данных о производительности портфеля
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о производительности портфеля")
public class PortfolioPerformanceDTO {

    @Schema(description = "Идентификатор портфеля", example = "1")
    private Long portfolioId;

    @Schema(description = "Текущая стоимость портфеля", example = "10500.25")
    private BigDecimal currentValue;

    @Schema(description = "Общая сумма инвестиций", example = "10000.00")
    private BigDecimal totalInvested;

    @Schema(description = "Общая прибыль/убыток в абсолютном выражении", example = "500.25")
    private BigDecimal profitLoss;

    @Schema(description = "Общая прибыль/убыток в процентном выражении", example = "5.00")
    private BigDecimal profitLossPercent;

    @Schema(description = "Распределение активов по секторам (ключ - сектор, значение - процент)")
    private Map<String, BigDecimal> sectorAllocation;

    @Schema(description = "Распределение активов по акциям (ключ - символ акции, значение - процент)")
    private Map<String, BigDecimal> stockAllocation;

    @Schema(description = "Данные для графика стоимости портфеля (значения в хронологическом порядке)")
    private List<PortfolioValueDataPoint> historicalValues;

    @Schema(description = "Топ прибыльных позиций")
    private List<HoldingPerformanceDTO> topGainers;

    @Schema(description = "Топ убыточных позиций")
    private List<HoldingPerformanceDTO> topLosers;

    /**
     * Вложенный класс для данных о стоимости портфеля в определенную дату
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortfolioValueDataPoint {
        @Schema(description = "Дата", example = "2023-06-15")
        private String date;

        @Schema(description = "Стоимость портфеля на указанную дату", example = "10250.50")
        private BigDecimal value;
    }

    /**
     * Вложенный класс для данных о производительности позиции
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HoldingPerformanceDTO {
        @Schema(description = "Символ акции", example = "AAPL")
        private String symbol;

        @Schema(description = "Название компании", example = "Apple Inc.")
        private String name;

        @Schema(description = "Количество акций", example = "10")
        private Integer quantity;

        @Schema(description = "Текущая стоимость позиции", example = "1525.00")
        private BigDecimal currentValue;

        @Schema(description = "Прибыль/убыток в абсолютном выражении", example = "225.00")
        private BigDecimal profitLoss;

        @Schema(description = "Прибыль/убыток в процентном выражении", example = "17.30")
        private BigDecimal profitLossPercent;
    }
}
