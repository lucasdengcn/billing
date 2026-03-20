package com.github.lucasdengcn.billing.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeatureType {
    API_ACCESS("api_access"),
    STORAGE_SPACE("storage_space"),
    SUPPORT_LEVEL("support_level"),
    BANDWIDTH_LIMIT("bandwidth_limit"),
    USER_LIMIT("user_limit"),
    CUSTOMIZATION("customization"),
    INTEGRATION("integration"),
    ANALYTICS("analytics"),
    SECURITY("security"),
    BACKUP("backup"),
    MONITORING("monitoring"),
    TOKEN("token"),
    CREDIT("credit"),
    OTHER("other");
    
    private final String value;
    
    public static FeatureType fromValue(String value) {
        for (FeatureType type : FeatureType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return OTHER; // default fallback
    }
}