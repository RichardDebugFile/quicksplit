package com.quicksplit.common;

import org.springframework.http.HttpStatus;

/** Conflicto con el estado actual, ej. email ya registrado (HTTP 409). */
public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
