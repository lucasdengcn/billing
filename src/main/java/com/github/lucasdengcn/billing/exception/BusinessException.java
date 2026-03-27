package com.github.lucasdengcn.billing.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
  private final HttpStatus httpStatus;

  public BusinessException(String message) {
    super(message);
    this.httpStatus = HttpStatus.BAD_REQUEST;
  }

  public BusinessException(String message, HttpStatus httpStatus) {
    super(message);
    this.httpStatus = httpStatus;
  }
}
