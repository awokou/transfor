package com.server.transfor.exception;


public class EmptySourceFileException extends RuntimeException {
    public EmptySourceFileException(String message) {
        super(message);
    }
}