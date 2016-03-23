package com.company.life_simulator.dweller.ant;

import com.company.life_simulator.dweller.Dweller;
import com.company.life_simulator.dweller.DwellerType;
import com.company.life_simulator.dweller.Food;
import com.company.life_simulator.dweller.ant.ai.implementation.AngleDistanceAI;
import com.company.life_simulator.util.StreamUtil;
import com.company.life_simulator.world.quadtree.Point;
import com.company.life_simulator.world.quadtree.Vector;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 5, jvmArgs = {"-XX:+AggressiveOpts"})
@Warmup(iterations = 10)
@Measurement(iterations = 10)
public class DirectionDecisionBenchmark {
    private Ant self = new Ant(0, new Point(0, 0), 0, 0);
    private final int points = 100;
    private Random random = new Random(777);
    private List<Dweller> dwellers = new ArrayList<>();

    @Setup
    public void init()
    {
        Stream<Double> xStream = random.doubles(points, -10, 10).boxed();
        Stream<Double> yStream = random.doubles(points, -10, 10).boxed();
        Stream<DwellerType> typeStream = Stream.concat(Stream.generate(() -> DwellerType.ant).limit(30), Stream.generate(() -> DwellerType.food).limit(70));
        dwellers = StreamUtil.zip(
                StreamUtil.zip(xStream, yStream, Point::new),
                typeStream,
                (point, dwellerType) -> {
                    if (dwellerType == DwellerType.ant)
                        return new Ant(0, point, 0, 0);
                    else
                        return new Food(0, point, 0);
                })
                .collect(Collectors.toList());
    }

    @Benchmark
    public Vector directionDecisionBenchmark()
    {
        return AngleDistanceAI.chooseDirection(self.getPosition(), dwellers).get();
    }
}