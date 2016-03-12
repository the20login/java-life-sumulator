package com.company.life_simulator.world.quadtree;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 5, jvmArgs = {"-XX:+AggressiveOpts"})
@Warmup(iterations = 10)
@Measurement(iterations = 10)
public class PointBenchmark {
    private Point point1;
    private Point point2;
    private double radius;

    @Setup
    public void init()
    {
        point1 = new Point(10, 10);
        point2 = new Point(20, 20);
        radius = 15;
    }

    @Benchmark
    public double distanceBenchmark()
    {
        return point1.squareDistance(point2);
    }

    @Benchmark
    public boolean inCircleBenchmark()
    {
        return point1.withinCircle(point2, radius);
    }
}