package com.tradingsystem.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи данных ответа на аутентификацию
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ на успешную аутентификацию")
public class JwtAuthResponseDTO {

    @Schema(description = "JWT токен для авторизации", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Тип токена", example = "Bearer")
    private String tokenType = "Bearer";

    public JwtAuthResponseDTO(String accessToken) {
        this.accessToken = accessToken;
    }
}
