package com.severityone.time;

import java.io.Serializable;
import java.time.*;
import java.time.temporal.*;
import java.util.Objects;

import static java.time.temporal.ChronoField.*;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;

public final class SimpleLocalTIme implements Comparable<SimpleLocalTIme>, Temporal, TemporalAdjuster, Serializable
{
    private static final long serialVersionUID = 9104340065704550565L;

    public static final SimpleLocalTIme MIN;
    public static final SimpleLocalTIme MAX;
    public static final SimpleLocalTIme MIDNIGHT;
    public static final SimpleLocalTIme NOON;

    private static final ValueRange HOUR_RANGE = HOUR_OF_DAY.range();
    private static final int HOUR_MIN = (int) HOUR_RANGE.getMinimum();
    private static final int HOUR_MAX = (int) HOUR_RANGE.getMaximum();
    private static final int HOURS_PER_DAY = HOUR_MAX - HOUR_MIN + 1;
    private static final int HOURS_PER_HALF_DAY = HOURS_PER_DAY >>> 1;

    private static final ValueRange MINUTE_RANGE = MINUTE_OF_DAY.range();
    private static final int MINUTE_MIN = (int) MINUTE_RANGE.getMinimum();
    private static final int MINUTE_MAX = (int) MINUTE_RANGE.getMaximum();
    private static final int MINUTES_PER_HOUR = MINUTE_MAX - MINUTE_MIN + 1;
    private static final int MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY;

    private static final ValueRange SECOND_RANGE = SECOND_OF_DAY.range();
    private static final int SECOND_MIN = (int) SECOND_RANGE.getMinimum();
    private static final int SECOND_MAX = (int) SECOND_RANGE.getMaximum();
    private static final int SECONDS_PER_MINUTE = SECOND_MAX - SECOND_MIN + 1;
    private static final int SECONDS_PER_DAY = SECONDS_PER_MINUTE * MINUTES_PER_HOUR * HOURS_PER_DAY;

    private static final ValueRange NANO_RANGE = NANO_OF_SECOND.range();
    private static final int NANO_MIN = (int) NANO_RANGE.getMinimum();
    private static final int NANO_MAX = (int) NANO_RANGE.getMaximum();
    private static final int NANOS_PER_SECOND = NANO_MAX - NANO_MIN + 1;

    private static final SimpleLocalTIme[] WHOLE_HOURS = new SimpleLocalTIme[HOURS_PER_DAY];

    static
    {
        for (int index = 0; index < HOURS_PER_DAY; index++)
            WHOLE_HOURS[index] = new SimpleLocalTIme(index, 0);
        MIDNIGHT = WHOLE_HOURS[0];
        NOON = WHOLE_HOURS[HOURS_PER_HALF_DAY];
        MIN = WHOLE_HOURS[0];
        MAX = new SimpleLocalTIme(HOUR_MAX, MINUTE_MAX);
    }

    private final byte hour;
    private final byte minute;

    private SimpleLocalTIme(final int hour, final int minute)
    {
        this.hour = (byte) hour;
        this.minute = (byte) minute;
    }

    private static SimpleLocalTIme create(final int hour, final int minute)
    {
        if (minute == 0)
            return WHOLE_HOURS[hour];
        else
            return new SimpleLocalTIme(hour, minute);
    }

    public static SimpleLocalTIme now()
    {
        return now(Clock.systemDefaultZone());
    }

    public static SimpleLocalTIme now(final ZoneId zoneId)
    {
        return now(Clock.system(zoneId));
    }

    public static SimpleLocalTIme now(final Clock clock)
    {
        Objects.requireNonNull(clock);
        final Instant now = clock.instant();
        return ofInstant(now, clock.getZone());
    }

    public static SimpleLocalTIme ofMinuteOfDay(final long minuteOfDay)
    {
        return new SimpleLocalTIme(
                (int) minuteOfDay / MINUTES_PER_HOUR,
                (int) minuteOfDay % MINUTES_PER_HOUR);
    }

    public static SimpleLocalTIme of(final int hour, final int minute)
    {
        HOUR_OF_DAY.checkValidValue(hour);
        if (minute == 0)
            return WHOLE_HOURS[hour];
        else
        {
            MINUTE_OF_HOUR.checkValidValue(minute);
            return new SimpleLocalTIme(hour, minute);
        }
    }

    public static SimpleLocalTIme ofInstant(final Instant instant, final ZoneId zoneId)
    {
        Objects.requireNonNull(instant, "instant");
        Objects.requireNonNull(zoneId, "zoneId");
        final ZoneOffset zoneOffset = zoneId.getRules().getOffset(instant);
        final long localSecond = instant.getEpochSecond() + zoneOffset.getTotalSeconds();
        final long minuteOfDay = (localSecond - Math.floorDiv(localSecond, (long) SECONDS_PER_DAY) * SECONDS_PER_DAY) / SECONDS_PER_MINUTE;
        return ofMinuteOfDay(minuteOfDay + instant.getNano() / NANOS_PER_SECOND);
    }

    public static SimpleLocalTIme from(final TemporalAccessor temporal)
    {
        Objects.requireNonNull(temporal);
        final SimpleLocalTIme time = temporal.query(
                t -> temporal.isSupported(SECOND_OF_DAY)
                        ? ofMinuteOfDay(temporal.getLong(SECOND_OF_DAY))
                        : null);
        if (time == null)
            throw new DateTimeException("Unable to obtain SimpleLocalTIme from TemporalAccessor: " +
                                        temporal + " of type " + temporal.getClass().getName());
        else
            return time;
    }

    public int getHour()
    {
        return hour;
    }

    public int getMinute()
    {
        return minute;
    }

    @Override
    public long getLong(final TemporalField field)
    {
        if (field instanceof ChronoField)
            return get((ChronoField) field);
        else
            return field.getFrom(this);
    }

    @Override
    public int get(final TemporalField field)
    {
        if (field instanceof ChronoField)
            return get((ChronoField) field);
        else
            return Temporal.super.get(field);
    }

    private int get(final ChronoField field)
    {
        switch (field)
        {
            case HOUR_OF_DAY:
                return hour;
            case MINUTE_OF_HOUR:
                return minute;
            case MINUTE_OF_DAY:
                return hour * HOURS_PER_DAY + minute;
            case HOUR_OF_AMPM:
                return hour % (HOURS_PER_HALF_DAY);
            case CLOCK_HOUR_OF_DAY:
                return hour == 0 ? 24 : hour;
            case CLOCK_HOUR_OF_AMPM:
                int clockHour = hour % (HOURS_PER_HALF_DAY);
                return clockHour == 0 ? 12 : clockHour;
            case AMPM_OF_DAY:
                return hour / HOURS_PER_HALF_DAY;
            default:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    public int toMinuteOfDay()
    {
        return hour * MINUTES_PER_HOUR + minute;
    }

    @Override
    public Temporal with(final TemporalAdjuster adjuster)
    {
        if (adjuster instanceof SimpleLocalTIme)
            return (SimpleLocalTIme) adjuster;
        else
            return adjuster.adjustInto(this);
    }

    @Override
    public Temporal with(final TemporalField field, final long newValue)
    {
        if (field instanceof ChronoField)
        {
            ((ChronoField) field).checkValidIntValue(newValue);
            switch ((ChronoField) field)
            {
                case HOUR_OF_DAY:
                    return withHour((int) newValue);
                case MINUTE_OF_HOUR:
                    return withMinute((int) newValue);
                case MINUTE_OF_DAY:
                    return WHOLE_HOURS[(int) newValue / MINUTES_PER_HOUR].withMinute((int) newValue % MINUTES_PER_HOUR);
                default:
                    throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
            }
        }
        else
        {
            return field.adjustInto(this, newValue);
        }
    }

    public SimpleLocalTIme withHour(final int hour)
    {
        if (hour == this.hour)
            return this;
        else
        {
            HOUR_OF_DAY.checkValidIntValue(hour);
            return create(hour, minute);
        }
    }

    public SimpleLocalTIme withMinute(final int minute)
    {
        if (minute == this.minute)
            return this;
        else
        {
            MINUTE_OF_HOUR.checkValidIntValue(minute);
            return create(hour, minute);
        }
    }

    @Override
    public Temporal plus(final TemporalAmount amount)
    {
        Objects.requireNonNull(amount);
        return amount.addTo(this);
    }

    @Override
    public Temporal plus(final long amountToAdd, final TemporalUnit unit)
    {
        Objects.requireNonNull(unit);
        if (unit instanceof ChronoUnit)
        {
            switch ((ChronoUnit) unit)
            {
                case MINUTES:
                    return plusMinutes(amountToAdd);
                case HOURS:
                    return plusHours(amountToAdd);
                case HALF_DAYS:
                    return plusHours((amountToAdd % 2) * (HOURS_PER_HALF_DAY));
                default:
                    throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
            }
        }
        else
        {
            return unit.addTo(this, amountToAdd);
        }
    }

    public SimpleLocalTIme plusHours(final long hoursToAdd)
    {
        if (hoursToAdd == 0L)
            return this;
        else
        {
            final int newHour = ((int) (hoursToAdd % HOURS_PER_DAY) + hour + HOURS_PER_DAY) % HOURS_PER_DAY;
            return create(newHour, minute);
        }
    }

    public SimpleLocalTIme plusMinutes(final long minutesToAdd)
    {
        if (minutesToAdd == 0)
            return this;
        else
        {
            final int minOfDay = hour * MINUTES_PER_HOUR + minute;
            final int newMinOfDay =
                    ((int) (minutesToAdd % MINUTES_PER_HOUR) + minOfDay + MINUTES_PER_HOUR) % MINUTES_PER_HOUR;
            if (newMinOfDay == minOfDay)
                return this;
            else
                return create(newMinOfDay / MINUTES_PER_HOUR, newMinOfDay % MINUTES_PER_HOUR);
        }
    }

    public SimpleLocalTIme minusHours(final long hoursToSubract)
    {
        return plusHours(-(hoursToSubract % HOURS_PER_DAY));
    }

    public SimpleLocalTIme minusMinutes(final long minutesToSubtract)
    {
        return plusMinutes(-(minutesToSubtract % MINUTES_PER_DAY));
    }

    @Override
    public long until(final Temporal endExclusive, final TemporalUnit unit)
    {
        Objects.requireNonNull(endExclusive);
        Objects.requireNonNull(unit);
        final SimpleLocalTIme end = SimpleLocalTIme.from(endExclusive);
        if (unit instanceof ChronoUnit)
        {
            final long minutesUntil = end.toMinuteOfDay() - toMinuteOfDay();
            switch ((ChronoUnit) unit)
            {
                case MINUTES:
                    return minutesUntil;
                case HOURS:
                    return minutesUntil / MINUTES_PER_HOUR;
                default:
                    throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
            }
        }
        return unit.between(this, end);
    }

    @Override
    public boolean isSupported(final TemporalUnit unit)
    {
        return unit == HOURS ||
               unit == MINUTES ||
               (unit != null && unit.isSupportedBy(this));
    }

    @Override
    public boolean isSupported(final TemporalField field)
    {
        return field == HOUR_OF_DAY ||
               field == MINUTE_OF_HOUR ||
               field == MINUTE_OF_DAY ||
               (field != null && field.isSupportedBy(this));
    }

    @Override
    public Temporal adjustInto(final Temporal temporal)
    {
        return temporal.with(MINUTE_OF_DAY, toMinuteOfDay());
    }

    @Override
    public int compareTo(final SimpleLocalTIme other)
    {
        Objects.requireNonNull(other);
        int result = Byte.compare(hour, other.hour);
        if (result == 0)
            result = Byte.compare(minute, other.minute);
        return result;
    }

    public boolean isAfter(final SimpleLocalTIme other)
    {
        return compareTo(other) > 0;
    }

    public boolean isBefore(final SimpleLocalTIme other)
    {
        return compareTo(other) < 0;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (this == other)
            return true;
        else
        {
            final SimpleLocalTIme time = (SimpleLocalTIme) other;
            return hour == time.hour && minute == time.minute;
        }
    }

    @Override
    public int hashCode()
    {
        final long minuteOfDay = toMinuteOfDay();
        return (int) (minuteOfDay ^ (minuteOfDay << 8));
    }

    @Override
    public String toString()
    {
        final StringBuilder string = new StringBuilder();
        if (hour < 10)
            string.append('0');
        string.append(hour).append(':');
        if (minute < 10)
            string.append('0');
        return string.append(minute).toString();
    }
}
