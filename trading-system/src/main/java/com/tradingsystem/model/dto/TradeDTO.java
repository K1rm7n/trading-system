package com.tradingsystem.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи данных о торговой операции
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о торговой операции")
public class TradeDTO {

    @Schema(description = "Идентификатор сделки", example = "1")
    private Long id;

    @NotNull
    @Schema(description = "Идентификатор портфеля", example = "1")
    private Long portfolioId;

    @NotNull
    @Schema(description = "Идентификатор акции", example = "1")
    private Long stockId;

    @Schema(description = "Символ акции", example = "AAPL")
    private String stockSymbol;

    @NotNull
    @Schema(description = "Тип операции (BUY, SELL)", example = "BUY")
    private String type;

    @NotNull
    @Min(1)
    @Schema(description = "Количество акций", example = "10")
    private Integer quantity;

    @NotNull
    @Schema(description = "Цена за акцию", example = "150.25")
    private BigDecimal price;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Время совершения сделки", example = "2023-06-15 14:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Общая стоимость сделки", example = "1502.50")
    private BigDecimal totalValue;
}
