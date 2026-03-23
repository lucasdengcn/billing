package com.github.lucasdengcn.billing.component;

import com.github.lucasdengcn.billing.entity.Subscription;
import com.github.lucasdengcn.billing.entity.enums.PeriodUnit;

import java.time.Duration;
import java.time.OffsetDateTime;

public class TimeDurations {

    public static int translateDurationToUnits(Duration duration, PeriodUnit periodUnit) {
        return switch (periodUnit) {
            case YEARS -> Math.toIntExact(Math.max(1, duration.toDays() / 365));
            case MONTHS -> Math.toIntExact(Math.max(1, duration.toDays() / 30));
            default -> 1;
        };
    }

    public static int translateDurationToUnits(Subscription subscription) {
        // time based subscription calculation
        // Calculate periods based on duration between start and end dates
        java.time.Duration duration = java.time.Duration.between(subscription.getStartDate(), subscription.getEndDate());
        return translateDurationToUnits(duration, subscription.getPeriodUnit());
    }

    public static int translateDurationToUnits(OffsetDateTime startDate, OffsetDateTime endDate, PeriodUnit periodUnit) {
        // time based subscription calculation
        // Calculate periods based on duration between start and end dates
        java.time.Duration duration = java.time.Duration.between(startDate, endDate);
        return translateDurationToUnits(duration, periodUnit);
    }

    public static OffsetDateTime calculateEndDate(OffsetDateTime currentEndDate, int periods, PeriodUnit periodUnit) {
        return switch (periodUnit) {
            case DAYS -> currentEndDate.plusDays(periods);
            case WEEKS -> currentEndDate.plusWeeks(periods);
            case MONTHS -> currentEndDate.plusMonths(periods);
            case YEARS -> currentEndDate.plusYears(periods);
            default -> currentEndDate.plusMonths(periods); // Default to months
        };
    }

    public static OffsetDateTime renewalEndDate(OffsetDateTime currentEndDate, int periods, PeriodUnit periodUnit) {
        if (currentEndDate.isBefore(OffsetDateTime.now())) {
            return calculateEndDate(OffsetDateTime.now(), periods, periodUnit);
        }
        return calculateEndDate(currentEndDate, periods, periodUnit);
    }

}
