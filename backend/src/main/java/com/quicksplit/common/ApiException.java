package com.quicksplit.common;

import org.springframework.http.HttpStatus;

/**
 * Excepcion base de la aplicacion que lleva asociado un codigo HTTP.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
