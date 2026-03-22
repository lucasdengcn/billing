package com.github.lucasdengcn.billing.exception;

import org.springframework.http.HttpStatus;

public class InvalidSubscriptionDateRangeException extends BusinessException {
    public InvalidSubscriptionDateRangeException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
    
    public InvalidSubscriptionDateRangeException() {
        super("Start date must be before end date", HttpStatus.BAD_REQUEST);
    }
}
