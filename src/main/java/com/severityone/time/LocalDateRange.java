package com.severityone.time;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.ValueRange;

public final class LocalDateRange extends AbstractTemporalRange<LocalDate>
{
    private LocalDateRange(final LocalDate startInclusive, final LocalDate endExclusive)
    {
        super(startInclusive, endExclusive, 1, ChronoUnit.DAYS);
    }

    public static LocalDateRange of(final LocalDate startInclusive, final LocalDate endExclusive) {
        return new LocalDateRange(startInclusive, endExclusive);
    }
}
