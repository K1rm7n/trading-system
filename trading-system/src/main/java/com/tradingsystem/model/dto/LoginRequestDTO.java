package com.tradingsystem.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи данных запроса на аутентификацию
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на вход в систему")
public class LoginRequestDTO {

    @NotBlank
    @Size(min = 3, max = 50)
    @Schema(description = "Имя пользователя (логин)", example = "john_doe")
    private String username;

    @NotBlank
    @Size(min = 6, max = 100)
    @Schema(description = "Пароль", example = "password123")
    private String password;
}
