package com.company.life_simulator.world;

import com.company.life_simulator.dweller.Dweller;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Fork(value = 5, jvmArgs = {"-XX:+AggressiveOpts"})
@Warmup(iterations = 10)
@Measurement(iterations = 10)
public class WorldBenchmark {
    private World world;

    @Setup
    public void loadProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(WorldBenchmark.class.getResourceAsStream("/world_benchmark.properties"));
        properties.stringPropertyNames().stream()
                .filter(name -> System.getProperty(name) == null)
                .forEach(name -> System.setProperty(name, properties.getProperty(name)));
    }

    @Setup(Level.Iteration)
    public void init()
    {
        world = WorldBuilder.createWorld(800);
    }

    @Benchmark
    public Stream<Dweller> tickBenchmark()
    {
        world.tick();
        return world.getDwellers();
    }

    /*@Test
    public void tickTest()
    {
        for (int i = 0; i< 10; i++) {
            world.tick();
            System.out.println(world.getDwellers().count());
        }
    }*/

}