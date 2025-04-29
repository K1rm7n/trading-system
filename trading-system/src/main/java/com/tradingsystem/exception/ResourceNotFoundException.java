package com.tradingsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при запросе ресурса, который не существует
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Создает новое исключение с указанным сообщением
     *
     * @param message Сообщение об ошибке
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Создает новое исключение с указанным сообщением и причиной
     *
     * @param message Сообщение об ошибке
     * @param cause Причина исключения
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
