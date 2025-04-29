package com.tradingsystem.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи данных об акции
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация об акции")
public class StockDTO {

    @Schema(description = "Идентификатор акции", example = "1")
    private Long id;

    @NotBlank
    @Size(min = 1, max = 20)
    @Schema(description = "Символ (тикер) акции", example = "AAPL")
    private String symbol;

    @NotBlank
    @Schema(description = "Название компании", example = "Apple Inc.")
    private String name;

    @Schema(description = "Сектор экономики", example = "Technology")
    private String sector;

    @Schema(description = "Индустрия", example = "Consumer Electronics")
    private String industry;

    @Schema(description = "Текущая цена акции", example = "150.25")
    private BigDecimal currentPrice;

    @Schema(description = "Цена закрытия предыдущего торгового дня", example = "148.30")
    private BigDecimal previousClose;

    @Schema(description = "Процент изменения цены за день", example = "1.32")
    private BigDecimal dayChangePercent;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Время последнего обновления данных", example = "2023-06-15 14:30:00")
    private LocalDateTime lastUpdated;
}
