package com.test.demo.exceptions.databaseException;

import org.springframework.http.HttpStatus;

public class DatabaseException extends RuntimeException {
    private final HttpStatus httpStatus;

    public DatabaseException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}

