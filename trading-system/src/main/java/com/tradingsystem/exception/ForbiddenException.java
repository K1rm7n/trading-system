package com.tradingsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при доступе к запрещенным ресурсам
 */
@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Создает новое исключение с указанным сообщением
     *
     * @param message Сообщение об ошибке
     */
    public ForbiddenException(String message) {
        super(message);
    }

    /**
     * Создает новое исключение с указанным сообщением и причиной
     *
     * @param message Сообщение об ошибке
     * @param cause Причина исключения
     */
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}