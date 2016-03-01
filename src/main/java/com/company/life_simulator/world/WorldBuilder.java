package com.company.life_simulator.world;

import com.company.life_simulator.dweller.Food;
import com.company.life_simulator.dweller.ant.Ant;
import com.company.life_simulator.util.StreamUtil;
import com.company.life_simulator.world.quadtree.Point;

import java.util.Random;
import java.util.stream.Stream;

public class WorldBuilder {
    public static World createWorld()
    {
        double width = Double.valueOf(System.getProperty("world.width", "200"));
        double height = Double.valueOf(System.getProperty("world.height", "200"));
        int initialFood = Integer.valueOf(System.getProperty("world.initialFood", "30"));

        World world = new World(width, height);

        world.addDweller(new Ant(world.getNextId(), new Point(width/2, height/2), 0, Ant.FOOD_SATURATION / 2));

        Random random = new Random();
        Stream<Double> xStream = random.doubles(initialFood, 0, width).boxed();
        Stream<Double> yStream = random.doubles(initialFood, 0, height).boxed();
        Stream<Integer> tickStream = random.ints(initialFood, -Food.REPRODUCTION_RATE, 0).boxed();

        StreamUtil.zip(
                StreamUtil.zip(xStream, yStream, Point::new),
                tickStream,
                (point, integer) -> new Food(world.getNextId(), point, integer))
                .forEach(world::addDweller);
        return world;
    }
}
