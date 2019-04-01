package com.severityone.time;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class LocalDateRangeTest
{
    @Test
    public void testIterator()
    {
        final LocalDate startDate = LocalDate.of(2019, Month.JANUARY, 1);
        final LocalDate endDate = startDate.plusDays(20);
        final LocalDateRange range = LocalDateRange.of(startDate, endDate);

        final long expected = IntStream.range(startDate.getDayOfMonth(), endDate.getDayOfMonth()).sum();
        long actual = 0;
        for (final LocalDate date : range)
            actual += date.getDayOfMonth();

        assertEquals(expected, actual);
    }

    @Test
    public void testSpliterator()
    {
        final LocalDate startDate = LocalDate.of(2019, Month.JANUARY, 1);
        final LocalDate endDate = startDate.plusDays(20);
        final LocalDateRange range = LocalDateRange.of(startDate, endDate);

        final long size = ChronoUnit.DAYS.between(startDate, endDate);
        final Spliterator<LocalDate> spliterator = range.spliterator();
        assertEquals(size, spliterator.estimateSize());
        assertEquals(size, spliterator.getExactSizeIfKnown());

        final Spliterator newSpliterator = spliterator.trySplit();
        assertEquals(size, spliterator.getExactSizeIfKnown() + newSpliterator.getExactSizeIfKnown());

        final long expected = IntStream.range(startDate.getDayOfMonth(), endDate.getDayOfMonth()).sum();
        final AtomicLong actual = new AtomicLong(0);
        StreamSupport.stream(range.spliterator(), true)
                     .forEach(date -> actual.addAndGet(date.getDayOfMonth()));
        assertEquals(expected, actual.get());
    }
}