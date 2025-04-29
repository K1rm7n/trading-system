package com.tradingsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при некорректных данных в запросе
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidRequestException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Создает новое исключение с указанным сообщением
     *
     * @param message Сообщение об ошибке
     */
    public InvalidRequestException(String message) {
        super(message);
    }

    /**
     * Создает новое исключение с указанным сообщением и причиной
     *
     * @param message Сообщение об ошибке
     * @param cause Причина исключения
     */
    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
