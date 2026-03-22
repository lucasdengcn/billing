package com.github.lucasdengcn.billing.exception;

import org.springframework.http.HttpStatus;

public class CustomerResolutionException extends BusinessException {
    public CustomerResolutionException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
    
    public CustomerResolutionException() {
        super("Could not resolve or create customer from provided information", HttpStatus.BAD_REQUEST);
    }
}
