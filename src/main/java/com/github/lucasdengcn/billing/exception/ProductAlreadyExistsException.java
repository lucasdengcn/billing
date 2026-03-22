package com.github.lucasdengcn.billing.exception;

import org.springframework.http.HttpStatus;

public class ProductAlreadyExistsException extends BusinessException {

    public ProductAlreadyExistsException(String productNo) {
        super(String.format("Product with product number '%s' already exists", productNo), HttpStatus.CONFLICT);
    }
}
