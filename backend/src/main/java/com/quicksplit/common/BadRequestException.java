package com.quicksplit.common;

import org.springframework.http.HttpStatus;

/** Peticion invalida por reglas de negocio (HTTP 400). */
public class BadRequestException extends ApiException {

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
