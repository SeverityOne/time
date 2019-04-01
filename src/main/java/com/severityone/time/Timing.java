package com.severityone.time;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;

public class Timing implements Comparable<Timing>, Temporal, TemporalAdjuster, Serializable
{
    private static final long serialVersionUID = 5278243574862218956L;

    private final LocalDate date;
    private final SimpleLocalTIme time;
    private final TimingType timingType;

    private Timing(final LocalDate date, final SimpleLocalTIme time, final TimingType timingType)
    {
        this.date = date;
        this.time = time;
        this.timingType = timingType;
    }

    @Override
    public int compareTo(final Timing other)
    {
        return 0;
    }

    @Override
    public boolean isSupported(final TemporalUnit unit)
    {
        return false;
    }

    @Override
    public boolean isSupported(final TemporalField field)
    {
        return false;
    }

    @Override
    public Temporal with(final TemporalField field, final long newValue)
    {
        return null;
    }

    @Override
    public Temporal plus(final long amountToAdd, final TemporalUnit unit)
    {
        return null;
    }

    @Override
    public long until(final Temporal endExclusive, final TemporalUnit unit)
    {
        return 0;
    }

    @Override
    public long getLong(final TemporalField field)
    {
        return 0;
    }

    @Override
    public Temporal adjustInto(final Temporal temporal)
    {
        return null;
    }
}
