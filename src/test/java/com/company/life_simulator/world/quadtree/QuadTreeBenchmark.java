package com.company.life_simulator.world.quadtree;

import com.company.life_simulator.util.StreamUtil;
import org.javatuples.Pair;
import org.junit.Before;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 5, jvmArgs = {"-XX:+AggressiveOpts"})
@Warmup(iterations = 10)
@Measurement(iterations = 10)
public class QuadTreeBenchmark {
    private QuadTree<Integer> quadTree;
    private Random random = new Random(777);
    private final double worldSize = 10000;
    private final int points = 100000;
    private final Point center = new Point(worldSize / 2, worldSize / 2);
    private final double radius = 1000;

    @Setup
    @Before
    public void init()
    {
        quadTree = new QuadTree<>(new Rectangle(0, 0, worldSize, worldSize));

        Stream<Double> xStream = random.doubles(points, 0, worldSize).boxed();
        Stream<Double> yStream = random.doubles(points, 0, worldSize).boxed();
        AtomicInteger counter = new AtomicInteger(0);
        StreamUtil.zip(xStream, yStream, Point::new)
                .forEach(point -> quadTree.put(point, counter.incrementAndGet()));
    }

    @Test
    public void circleIntersectTest()
    {
        Object[] list = null;
        for (int i = 0; i < 10;) {
            list = quadTree.searchWithin(center, radius)
                    .toArray();
        }
        System.out.println(list.length);
    }

    @Benchmark
    public Object[] rectangleIntersectBenchmark1()
    {
        return quadTree.searchWithin(new Rectangle(center.getX() - radius, center.getY() - radius, center.getX() + radius, center.getY() + radius))
                .toArray();
    }

    @Benchmark
    public Integer rectangleIntersectBenchmark2()
    {
        return quadTree.searchWithin(new Rectangle(center.getX() - radius, center.getY() - radius, center.getX() + radius, center.getY() + radius))
                .sorted(Integer::compareTo)
                .findFirst().get();
    }

    @Benchmark
    public Integer circleIntersectBenchmark()
    {
        return quadTree.searchWithin(center, radius)
                .sorted(Integer::compareTo)
                .findFirst().get();
    }
}