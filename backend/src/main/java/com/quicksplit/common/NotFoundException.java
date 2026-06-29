package com.quicksplit.common;

import org.springframework.http.HttpStatus;

/** Recurso inexistente (HTTP 404). */
public class NotFoundException extends ApiException {

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
