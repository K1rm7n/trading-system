package com.tradingsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, возникающее при ошибках во взаимодействии с внешними сервисами
 */
@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class ExternalServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Создает новое исключение с указанным сообщением
     *
     * @param message Сообщение об ошибке
     */
    public ExternalServiceException(String message) {
        super(message);
    }

    /**
     * Создает новое исключение с указанным сообщением и причиной
     *
     * @param message Сообщение об ошибке
     * @param cause Причина исключения
     */
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
