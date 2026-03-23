package com.github.lucasdengcn.billing.service;

import com.github.lucasdengcn.billing.entity.*;
import com.github.lucasdengcn.billing.model.request.FeatureUsageTrackingRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FeatureAccessService {
    /**
     * Track feature usage by device number, product number, and feature number
     */
    FeatureAccessLog trackFeatureUsage(FeatureUsageTrackingRequest request);
}