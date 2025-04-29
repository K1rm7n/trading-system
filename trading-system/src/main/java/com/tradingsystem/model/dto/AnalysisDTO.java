package com.tradingsystem.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи данных об анализе акции
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация об анализе акции")
public class AnalysisDTO {

    @Schema(description = "Идентификатор анализа", example = "1")
    private Long id;

    @Schema(description = "Идентификатор акции", example = "1")
    private Long stockId;

    @Schema(description = "Символ акции", example = "AAPL")
    private String stockSymbol;

    @Schema(description = "Тип тренда (UPTREND, DOWNTREND, SIDEWAYS)", example = "UPTREND")
    private String trend;

    @Schema(description = "Рекомендация (BUY, SELL, HOLD)", example = "BUY")
    private String recommendation;

    @Schema(description = "Уровень уверенности в рекомендации (от 0 до 1)", example = "0.85")
    private BigDecimal confidenceScore;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Дата и время создания анализа", example = "2023-06-15 14:30:00")
    private LocalDateTime analysisDate;

    @Schema(description = "Обоснование рекомендации", example = "Компания демонстрирует устойчивый рост...")
    private String rationale;
}
