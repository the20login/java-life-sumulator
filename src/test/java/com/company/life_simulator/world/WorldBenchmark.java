package com.company.life_simulator.world;

import com.company.life_simulator.dweller.Dweller;
import com.company.life_simulator.dweller.Food;
import com.company.life_simulator.dweller.ant.Ant;
import com.company.life_simulator.util.StreamUtil;
import com.company.life_simulator.world.quadtree.Point;
import org.junit.Before;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;
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
        properties.load(WorldBenchmark.class.getResourceAsStream("/application.properties"));
        properties.stringPropertyNames().stream()
                .filter(name -> System.getProperty(name) == null)
                .forEach(name -> System.setProperty(name, properties.getProperty(name)));
    }

    @Setup(Level.Iteration)
    public void init()
    {
        double width = Double.valueOf(System.getProperty("world.width", "200"));
        double height = Double.valueOf(System.getProperty("world.height", "200"));
        int initialFood = Integer.valueOf(System.getProperty("world.initialFood", "30"));

        world = new World(width, height, 800);

        world.addDweller(new Ant(world.getNextId(), new Point(width/2, height/2), 0, Ant.FOOD_SATURATION / 2));

        Random random = new Random(800);
        Stream<Double> xStream = random.doubles(initialFood, 0, width).boxed();
        Stream<Double> yStream = random.doubles(initialFood, 0, height).boxed();
        Stream<Integer> tickStream = random.ints(initialFood, -Food.REPRODUCTION_RATE, 0).boxed();

        StreamUtil.zip(
                StreamUtil.zip(xStream, yStream, Point::new),
                tickStream,
                (point, integer) -> new Food(world.getNextId(), point, integer))
                .forEach(world::addDweller);
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