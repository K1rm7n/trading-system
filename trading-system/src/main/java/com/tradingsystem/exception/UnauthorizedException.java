package com.tradingsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при неавторизованном доступе к ресурсам
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Создает новое исключение с указанным сообщением
     *
     * @param message Сообщение об ошибке
     */
    public UnauthorizedException(String message) {
        super(message);
    }

    /**
     * Создает новое исключение с указанным сообщением и причиной
     *
     * @param message Сообщение об ошибке
     * @param cause Причина исключения
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
