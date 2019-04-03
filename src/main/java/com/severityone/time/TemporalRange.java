package com.severityone.time;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This abstract class serves as a basis for classes that offer a convenient way to iterate through a set of
 * temporal values. An implementing class only needs to implement a (private) constructor and static factory
 * methods; the rest is taken care of by this class.
 *
 * @param <T> the type that this range iterates over, such as {@code LocalDate}
 */
public class TemporalRange<T extends Temporal & Comparable<? super T>> implements Iterable<T>
{
    private final T startInclusive;
    private final TemporalUnit unit;
    private final long amount;
    private final long size;

    /**
     * Constructs a new instance of {@code TemportalRange}. The end point can be before the starting point, in order
     * to iterate back in time, but then the amount must be negative. Similarly, the amount must be positive if
     * iteration goes forward in time.
     *
     * @param startInclusive the starting point in time, inclusive, from which to iterate
     * @param endExclusive   the end point in time, exclusive, to which to iterate
     * @param amount         the amount of temporal units to add for each iteration
     * @param unit           the tyoe of temporal units to add for each iteration
     * @throws NumberFormatException    if any of the non-primitive parameters is null
     * @throws IllegalArgumentException if the end point is before the starting point and the amount is positive,
     *                                  or if the end point is after the starting point and the amount is negative
     */
    protected TemporalRange(final T startInclusive,
                            final T endExclusive,
                            final long amount,
                            final TemporalUnit unit) throws NullPointerException, IllegalArgumentException
    {
        if (startInclusive.compareTo(endExclusive) * amount >= 0)
            throw new IllegalArgumentException("endless loop detected");
        this.startInclusive = Objects.requireNonNull(startInclusive);
        this.size = unit.between(startInclusive, Objects.requireNonNull(endExclusive));
        this.amount = amount;
        this.unit = Objects.requireNonNull(unit);
    }

    /**
     * Constructs a new instance of {@code TemportalRange}. If the size is negative (meaning, iterating back in time)
     * then the amount must be negative, too, and vice versa.
     *
     * @param startInclusive the starting point in time, inclusive, from which to iterate
     * @param size           the number of units added to the starting point, which marks the end point
     * @param amount         the amount of temporal units to add for each iteration
     * @param unit           the tyoe of temporal units to add for each iteration
     * @throws NumberFormatException    if any of the non-primitive parameters is null
     * @throws IllegalArgumentException if size is negative and the amount is positive, or if size is positive and
     *                                  the amount is negative
     */
    protected TemporalRange(final T startInclusive,
                            final long size,
                            final long amount,
                            final TemporalUnit unit)
    {
        this.startInclusive = Objects.requireNonNull(startInclusive);
        this.size = size;
        this.amount = amount;
        this.unit = Objects.requireNonNull(unit);
    }

    /**
     * Returns an new {@link Iterator} for this temporal range.
     *
     * @return an iterator that can be used in a loop, such as an enhanced for-loop
     */
    public Iterator<T> iterator()
    {
        return new RangeIterator();
    }

    /**
     * Returns a new {@link Spliterator} for this temporal range. This spliterator is {@code ORDERED}, {@code DISTINCT},
     * {@code SORTED}, {@code SIZED}, {@code NONNULL}, {@code IMMUTABLE}, and {@code SUBSIZED}.
     *
     * @return a spliterator for the range.
     */
    public Spliterator<T> spliterator()
    {
        return new RangeSpliterator(0, size);
    }

    /**
     * Returns a sequential {@link Stream} with this temporal range as its source.
     *
     * @return a sequential {@code Stream}
     */
    public Stream<T> stream()
    {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Returns a parallel {@link Stream} with this temporal range as its source.
     *
     * @return a parallel {@code Stream}
     */
    public Stream<T> parallelStream()
    {
        return StreamSupport.stream(spliterator(), true);
    }

    private class RangeIterator implements Iterator<T>
    {
        private long current = 0;

        @Override
        public boolean hasNext()
        {
            return current < size;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T next()
        {
            if (current >= size)
                throw new NoSuchElementException();
            final long old = current;
            current += amount;
            return (T) startInclusive.plus(old, unit);
        }
    }

    private class RangeSpliterator implements Spliterator<T>
    {
        private static final int CHARACTERISTICS = ORDERED | DISTINCT | SORTED | SIZED | NONNULL | IMMUTABLE | SUBSIZED;

        private long current;
        private long end;

        RangeSpliterator(final long start, final long end)
        {
            this.current = start;
            this.end = end;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean tryAdvance(final Consumer<? super T> action)
        {
            if (current >= end)
                return false;
            else
            {
                final long old = current;
                current += amount;
                action.accept((T) startInclusive.plus(old, unit));
                return true;
            }
        }

        @Override
        public Spliterator<T> trySplit()
        {
            final long mid = (end - current) >>> 1;
            if (mid <= current + 1)
                return null;
            else
            {
                final long oldEnd = end;
                end = mid;
                return new RangeSpliterator(mid, oldEnd);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void forEachRemaining(final Consumer<? super T> action)
        {
            for (long index = current; index < end; index += amount)
                action.accept((T) startInclusive.plus(index, unit));
        }

        @Override
        public long estimateSize()
        {
            return end - current;
        }

        @Override
        public long getExactSizeIfKnown()
        {
            return end - current;
        }

        @Override
        public int characteristics()
        {
            return CHARACTERISTICS;
        }

        @Override
        public boolean hasCharacteristics(final int characteristics)
        {
            return (characteristics & CHARACTERISTICS) == characteristics;
        }

        @Override
        public Comparator<? super T> getComparator()
        {
            return Comparator.naturalOrder();
        }
    }
}
