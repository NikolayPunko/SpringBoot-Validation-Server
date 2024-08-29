package com.host.SpringBootValidationServer.exceptions;

public class UnknownFieldTypeException extends RuntimeException {
    public UnknownFieldTypeException(String message) {
        super(message);
    }
}
