package com.quicksplit.common;

import org.springframework.http.HttpStatus;

/** El usuario no tiene permiso sobre el recurso (HTTP 403). */
public class ForbiddenException extends ApiException {

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
