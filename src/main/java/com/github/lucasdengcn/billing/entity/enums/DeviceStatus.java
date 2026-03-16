package com.github.lucasdengcn.billing.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeviceStatus {
    DEACTIVATED(0),
    ACTIVE(1);

    private final int value;

    public static DeviceStatus fromValue(int value) {
        for (DeviceStatus status : DeviceStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        return ACTIVE; // default
    }
}
