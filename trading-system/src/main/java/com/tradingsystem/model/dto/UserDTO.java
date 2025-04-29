package com.tradingsystem.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи данных о пользователе
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о пользователе")
public class UserDTO {

    @Schema(description = "Идентификатор пользователя", example = "1")
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Schema(description = "Имя пользователя (логин)", example = "john_doe")
    private String username;

    @NotBlank
    @Email
    @Schema(description = "Email пользователя", example = "john@example.com")
    private String email;

    @Schema(description = "Пароль пользователя")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
