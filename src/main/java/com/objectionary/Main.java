// SPDX-FileCopyrightText: Copyright (c) 2026 Objectionary.com
// SPDX-License-Identifier: MIT
package com.objectionary;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Benchmarks of the intermediate operations of {@link LongStream}, each applied
 * to an array of one million numbers and finished with a terminal sum.
 *
 * @since 0.0.1
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class Main {

    private final long[] numbers = LongStream.rangeClosed(1, 1_000_000).toArray();

    @Benchmark
    public long filter() {
        return this.verified(
            Arrays.stream(this.numbers).filter(number -> number % 2L == 0L).sum(),
            250_000_500_000L
        );
    }

    @Benchmark
    public long map() {
        return this.verified(
            Arrays.stream(this.numbers).map(number -> number * 2L).sum(),
            1_000_001_000_000L
        );
    }

    @Benchmark
    public long mapToObj() {
        return this.verified(
            Arrays.stream(this.numbers).mapToObj(Long::valueOf)
                .mapToLong(Long::longValue).sum(),
            500_000_500_000L
        );
    }

    @Benchmark
    public long mapToInt() {
        return this.verified(
            Arrays.stream(this.numbers).mapToInt(number -> (int) (number & 1L)).sum(),
            500_000L
        );
    }

    @Benchmark
    public long mapToDouble() {
        return this.verified(
            (long) Arrays.stream(this.numbers).mapToDouble(number -> (double) number).sum(),
            500_000_500_000L
        );
    }

    @Benchmark
    public long flatMap() {
        return this.verified(
            Arrays.stream(this.numbers).flatMap(LongStream::of).sum(),
            500_000_500_000L
        );
    }

    @Benchmark
    public long mapMulti() {
        return this.verified(
            Arrays.stream(this.numbers)
                .mapMulti((number, sink) -> sink.accept(number)).sum(),
            500_000_500_000L
        );
    }

    @Benchmark
    public long distinct() {
        return this.verified(
            Arrays.stream(this.numbers).distinct().sum(),
            500_000_500_000L
        );
    }

    @Benchmark
    public long sorted() {
        return this.verified(
            Arrays.stream(this.numbers).sorted().sum(),
            500_000_500_000L
        );
    }

    @Benchmark
    public long peek(final Blackhole blackhole) {
        return this.verified(
            Arrays.stream(this.numbers).peek(blackhole::consume).sum(),
            500_000_500_000L
        );
    }

    @Benchmark
    public long limit() {
        return this.verified(
            Arrays.stream(this.numbers).limit(100_000L).sum(),
            5_000_050_000L
        );
    }

    @Benchmark
    public long skip() {
        return this.verified(
            Arrays.stream(this.numbers).skip(100_000L).sum(),
            495_000_450_000L
        );
    }

    @Benchmark
    public long takeWhile() {
        return this.verified(
            Arrays.stream(this.numbers).takeWhile(number -> number <= 100_000L).sum(),
            5_000_050_000L
        );
    }

    @Benchmark
    public long dropWhile() {
        return this.verified(
            Arrays.stream(this.numbers).dropWhile(number -> number <= 100_000L).sum(),
            495_000_450_000L
        );
    }

    @Benchmark
    public long boxed() {
        return this.verified(
            Arrays.stream(this.numbers).boxed().mapToLong(Long::longValue).sum(),
            500_000_500_000L
        );
    }

    @Benchmark
    public long asDoubleStream() {
        return this.verified(
            (long) Arrays.stream(this.numbers).asDoubleStream().sum(),
            500_000_500_000L
        );
    }

    @Benchmark
    public long parallel() {
        return this.verified(
            Arrays.stream(this.numbers).parallel().sum(),
            500_000_500_000L
        );
    }

    private long verified(final long sum, final long expected) {
        if (sum != expected) {
            throw new IllegalStateException(
                String.format("the sum %d does not match the expected %d", sum, expected)
            );
        }
        return sum;
    }
}
