package com.github.lucasdengcn.billing.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DiscountStatus {
    INACTIVE(0),
    ACTIVE(1);

    private final int value;

    public static DiscountStatus fromValue(int value) {
        for (DiscountStatus status : DiscountStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        return INACTIVE; // default
    }
}
