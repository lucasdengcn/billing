package com.github.lucasdengcn.billing.exception;

import org.springframework.http.HttpStatus;

public class DeviceRegistrationException extends BusinessException {
    public DeviceRegistrationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
    
    public DeviceRegistrationException(String message, Throwable cause) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
