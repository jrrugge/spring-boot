package com.johnscode.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Overlapping ACTIVE booking → HTTP 409 */
@ResponseStatus(HttpStatus.CONFLICT)
public class CarAlreadyBookedException extends RuntimeException {

    public CarAlreadyBookedException(String message) {
        super(message);
    }
}
