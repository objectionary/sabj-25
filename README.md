# Stream API Benchmarks for Java 25

[![benchmark](https://github.com/objectionary/sabj25/actions/workflows/benchmark.yml/badge.svg)](https://github.com/objectionary/sabj25/actions/workflows/benchmark.yml)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/objectionary/sabj25/blob/master/LICENSE.txt)

This repository contains a set of benchmarks for [Stream API] in Java 25.

The motivation of this repository is the lack of benchmarks that would
  cover the entire set of terminal and non-terminal methods of
  Java Stream API, in their different combinations.
The benchmarks of [Biboudis et al.][biboudis2014] ([sources][cotl])
  and [Møller et al.][moller2020] ([sources][streamliner])
  only cover `map()` and `filter()` methods.
[Rosales et al.][rosales2023] go further with `StreamProf`, the first
  dedicated stream profiler for the JVM, yet they target the runtime
  overhead of `map()`, `filter()`, and `reduce()` rather than the
  breadth of the API.

A few open-source projects on GitHub benchmark streams as well,
  yet each of them stays narrow:
  [keaz/java-stream-benchmark][keaz] measures only `sort()` and
  `toString()` to tell when a parallel stream pays off,
  [Pask423/stream-benchmarks][pask423] studies the internals of
  parallel streams across a range of thread counts,
  [phonty29/stream-benchmarks][phonty29] compares streams against
  ordinary `for` loops,
  and [Nikolas-Charalambidis/java-16-mapmulti-benchmark][mapmulti]
  contrasts the `mapMulti()` method with `flatMap()`.
The [SoftwareMill][softwaremill] blog benchmarks a single
  log-processing pipeline of `map()`, `filter()`, and
  `collect(groupingBy())`, again touching only a handful of methods.
None of them exercise the full set of terminal and intermediate
  methods, let alone their combinations.

## Principles

The pipelines are built to measure the Stream API itself, not the
  cleverness of a particular JIT compiler. They follow a few rules:

- **No incidental repetition.** Where a pipeline sets out to cover the
  API, each method appears exactly once, so it measures the operation in
  combination with others rather than a loop of the same call.
  Repetition shows up only when it is the subject itself, as in
  `megamorphic`, which repeats `map()` and `filter()` on purpose to turn
  the call sites megamorphic.
- **No easy optimization hotspots.** A `Blackhole` observation sits
  after each boxing stage, because otherwise GraalVM's partial escape
  analysis scalar-replaces the boxed `Long` and `Double` values and the
  whole pipeline collapses to almost nothing, measuring elision instead
  of work.
- **Lambdas do real arithmetic.** No lambda is an identity function;
  every element flows through genuine computation at each step.
- **Every result is verified.** Each pipeline checks its sum against a
  precomputed constant, so a run that silently skipped work fails loudly
  rather than reporting a fast but wrong number.
- **The full API, in combination.** The pipelines span all four stream
  types, `long`, `int`, `double`, and object, and weave terminal and
  intermediate methods together, including `flatMap()` and `mapMulti()`.
- **One concern per pipeline.** `scalar` isolates one-to-one
  conversions, `stateless` every stateless operation, `stateful` the
  operations that must remember state, and `megamorphic` the megamorphic
  call sites.

## Results

The numbers come from [JMH][jmh] 1.37, the Java Microbenchmark Harness,
  driven from a JUnit test that builds the `Options` and runs the `Runner`
  programmatically rather than from the command line.
Every method is measured in `AverageTime` mode and reported in
  milliseconds per operation (`ms/op`), so each score is the mean wall-clock
  time of a single invocation of one `@Benchmark` method.
Because each pipeline processes a different element count and operation mix,
  the rows measure distinct workloads: compare JVMs down a single column,
  not one benchmark against another across rows.
Each benchmark runs in two forks (each a freshly started JVM), with three
  warmup iterations of one second each to let the JIT compiler settle,
  followed by five measurement iterations of two seconds each; the reported
  score is the arithmetic mean of all ten measurement iterations across the
  two forks.
The state is thread-scoped and JMH drives each benchmark from a single
  measurement thread, though the pipelines that call `parallel()` still fan
  their work across the shared fork-join pool; a `Blackhole` consumes
  intermediate boxed values to stop the JIT from eliminating the pipeline
  as dead code.
JMH writes the raw results to `target/jmh-result.csv`, which the CI
  pipeline parses into the table below.
The benchmarks run on every push to `master`, once per JVM, and the
  table is regenerated automatically:

<!-- benchmark_begin -->

| Benchmark | Temurin 25 | Zulu 25 | Corretto 25 | GraalVM 25 | Oracle 25 | Semeru 25 |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| `collection` | 11.830 | 16.128 | 15.979 | 7.265 | 15.268 | 25.325 |
| `collectors` | 36.680 | 47.796 | 47.344 | 42.595 | 47.091 | 52.460 |
| `combine` | 4.398 | 5.847 | 5.971 | 7.472 | 7.188 | 94.120 |
| `concurrent` | 159.943 | 210.353 | 205.004 | 185.043 | 187.805 | 290.952 |
| `craft` | 9.495 | 12.117 | 12.101 | 20.448 | 12.168 | 61.402 |
| `fanout` | 49.895 | 66.661 | 64.832 | 15.048 | 61.729 | 121.152 |
| `fold` | 18.706 | 23.005 | 24.128 | 13.432 | 22.653 | 49.761 |
| `forge` | 13.017 | 16.617 | 16.401 | 22.088 | 15.658 | 72.920 |
| `gatherer` | 18.141 | 22.154 | 22.107 | 22.780 | 21.094 | 43.654 |
| `generated` | 7.089 | 9.173 | 9.193 | 8.219 | 8.849 | 14.105 |
| `harvest` | 71.545 | 91.598 | 92.432 | 71.939 | 88.805 | 150.489 |
| `longlar` | 12.301 | 15.590 | 14.608 | 20.069 | 14.032 | 12.904 |
| `materialize` | 67.053 | 84.702 | 87.766 | 30.542 | 84.201 | 72.629 |
| `megamorphic` | 13.698 | 17.346 | 17.124 | 20.911 | 16.610 | 22.687 |
| `objects` | 53.673 | 69.178 | 67.288 | 78.597 | 66.592 | 81.001 |
| `overhead` | 0.000 | 0.000 | 0.000 | 0.000 | 0.000 | 0.001 |
| `parallel` | 4.888 | 6.531 | 6.543 | 5.790 | 6.622 | 7.444 |
| `random` | 12.050 | 15.552 | 15.544 | 13.545 | 14.050 | 18.748 |
| `reduction` | 3.216 | 4.166 | 4.171 | 1.837 | 3.925 | 6.657 |
| `scalar` | 7.820 | 10.068 | 10.192 | 11.639 | 9.936 | 22.571 |
| `shortcircuit` | 18.294 | 23.451 | 24.093 | 10.820 | 22.797 | 43.000 |
| `sources` | 4.448 | 5.585 | 5.726 | 6.986 | 5.195 | 26.387 |
| `spread` | 12.938 | 16.451 | 16.616 | 11.985 | 15.784 | 16.851 |
| `stateful` | 11.028 | 13.841 | 14.442 | 16.577 | 14.772 | 25.727 |
| `stateless` | 11.389 | 14.962 | 14.949 | 13.474 | 14.086 | 27.552 |
| `text` | 7.472 | 10.617 | 10.372 | 14.314 | 9.907 | 17.229 |
| `traverse` | 25.055 | 36.530 | 43.729 | 29.984 | 30.635 | 60.068 |
| `unordered` | 15.613 | 20.364 | 21.016 | 19.070 | 19.951 | 24.005 |

All scores are in milliseconds per operation (ms/op); lower is better.
The results were calculated in [this GHA job][benchmark-gha]
on 2026-06-09 at 19:45.
Each JVM ran on its own GitHub-hosted Linux runner,
so the scores across columns are indicative, not strictly comparable.
<!-- benchmark_end -->

[biboudis2014]: https://arxiv.org/abs/1406.6631
[cotl]: https://github.com/biboudis/clashofthelambdas
[moller2020]: https://dl.acm.org/doi/abs/10.1145/3428236
[streamliner]: https://github.com/cs-au-dk/streamliner
[keaz]: https://github.com/keaz/java-stream-benchmark
[pask423]: https://github.com/Pask423/stream-benchmarks
[phonty29]: https://github.com/phonty29/stream-benchmarks
[mapmulti]: https://github.com/Nikolas-Charalambidis/java-16-mapmulti-benchmark
[rosales2023]: https://arxiv.org/abs/2302.10006
[softwaremill]: https://softwaremill.com/benchmarking-java-streams/
[benchmark-gha]: https://github.com/objectionary/sabj25/actions/runs/27230653474
[jmh]: https://github.com/openjdk/jmh
[Stream API]: https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/stream/package-summary.html
