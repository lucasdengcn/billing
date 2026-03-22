package com.github.lucasdengcn.billing.exception;

import org.springframework.http.HttpStatus;

public class FeatureAccessLimitException extends BusinessException {

    public FeatureAccessLimitException(String featureName) {
        super(String.format("Access limit reached for feature: %s. Insufficient balance.", featureName), HttpStatus.TOO_MANY_REQUESTS);
    }
}
