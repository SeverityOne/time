package com.severityone.time;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;

public abstract class AbstractTemporalRange<T extends Temporal & Comparable<? super T>> implements Iterable<T>
{
    private final T startInclusive;
    private final TemporalUnit unit;
    private final long amount;
    private final long size;

    protected AbstractTemporalRange(final T startInclusive,
                                    final T endExclusive,
                                    final long amount,
                                    final TemporalUnit unit)
    {
        this.startInclusive = startInclusive;
        this.size = unit.between(startInclusive, endExclusive);
        this.amount = amount;
        this.unit = unit;
    }

    protected AbstractTemporalRange(final T startInclusive,
                                    final long size,
                                    final long amount,
                                    final TemporalUnit unit)
    {
        this.startInclusive = startInclusive;
        this.size = size;
        this.amount = amount;
        this.unit = unit;
    }

    public Iterator<T> iterator()
    {
        return new RangeIterator();
    }

    public Spliterator<T> spliterator()
    {
        return new RangeSpliterator(0, size);
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

        public RangeSpliterator(final long start, final long end)
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
