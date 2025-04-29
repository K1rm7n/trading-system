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
 * DTO для передачи данных о портфеле
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о портфеле")
public class PortfolioDTO {

    @Schema(description = "Идентификатор портфеля", example = "1")
    private Long id;

    @Schema(description = "Идентификатор пользователя", example = "1")
    private Long userId;

    @NotBlank
    @Size(min = 1, max = 100)
    @Schema(description = "Название портфеля", example = "Мой инвестиционный портфель")
    private String name;

    @Schema(description = "Описание портфеля", example = "Портфель для долгосрочных инвестиций")
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Дата создания портфеля", example = "2023-01-15 10:00:00")
    private LocalDateTime creationDate;

    @Schema(description = "Общая стоимость портфеля", example = "10000.00")
    private BigDecimal totalValue;
}
