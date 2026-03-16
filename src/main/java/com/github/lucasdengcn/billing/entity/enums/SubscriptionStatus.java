package com.github.lucasdengcn.billing.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionStatus {
    PENDING(0),
    ACTIVE(1),
    CANCELLED(2),
    EXPIRED(3);

    private final int value;

    public static SubscriptionStatus fromValue(int value) {
        for (SubscriptionStatus status : SubscriptionStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        return PENDING; // default
    }
}
