package com.github.lucasdengcn.billing.exception;

import org.springframework.http.HttpStatus;

public class SubscriptionAlreadyActiveException extends BusinessException {
    public SubscriptionAlreadyActiveException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
    
    public SubscriptionAlreadyActiveException(Long deviceId, Long productId) {
        super(String.format("Device %d already has an active subscription to product %d", deviceId, productId), HttpStatus.CONFLICT);
    }
}
