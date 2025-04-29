package com.tradingsystem.model.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи данных о позиции в портфеле
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о позиции в портфеле")
public class HoldingDTO {

    @Schema(description = "Идентификатор позиции", example = "1")
    private Long id;

    @Schema(description = "Идентификатор портфеля", example = "1")
    private Long portfolioId;

    @Schema(description = "Идентификатор акции", example = "1")
    private Long stockId;

    @Schema(description = "Символ акции", example = "AAPL")
    private String stockSymbol;

    @Schema(description = "Название компании", example = "Apple Inc.")
    private String stockName;

    @Schema(description = "Количество акций", example = "10")
    private Integer quantity;

    @Schema(description = "Средняя цена покупки", example = "130.00")
    private BigDecimal averagePrice;

    @Schema(description = "Текущая цена акции", example = "150.25")
    private BigDecimal currentPrice;

    @Schema(description = "Текущая стоимость позиции", example = "1502.50")
    private BigDecimal currentValue;

    @Schema(description = "Прибыль/убыток в абсолютном выражении", example = "202.50")
    private BigDecimal profitLoss;

    @Schema(description = "Прибыль/убыток в процентном выражении", example = "15.58")
    private BigDecimal profitLossPercent;

    @Schema(description = "Доля позиции в портфеле (в процентах)", example = "12.5")
    private BigDecimal allocationPercent;
}
