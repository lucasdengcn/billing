package com.github.lucasdengcn.billing.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    PENDING(0),
    PAID(1),
    OVERDUE(2),
    PARTIALLY_PAID(3);

    private final int value;

    public static PaymentStatus fromValue(int value) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        return PENDING; // default
    }
}
