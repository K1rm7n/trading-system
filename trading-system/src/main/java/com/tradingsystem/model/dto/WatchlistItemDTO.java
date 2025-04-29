package com.tradingsystem.model.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи данных об элементе списка отслеживания
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация об элементе списка отслеживания")
public class WatchlistItemDTO {

    @Schema(description = "Идентификатор элемента списка отслеживания", example = "1")
    private Long id;

    @Schema(description = "Идентификатор пользователя", example = "1")
    private Long userId;

    @Schema(description = "Идентификатор акции", example = "1")
    private Long stockId;

    @Schema(description = "Символ акции", example = "AAPL")
    private String stockSymbol;

    @Schema(description = "Название компании", example = "Apple Inc.")
    private String stockName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Дата добавления в список отслеживания", example = "2023-06-15 14:30:00")
    private LocalDateTime addedAt;

    @Schema(description = "Заметки пользователя", example = "Перспективная акция для долгосрочных инвестиций")
    private String notes;

    @Schema(description = "Целевая цена", example = "180.00")
    private Double priceTarget;
}
