package com.company.life_simulator;

import com.company.life_simulator.world.World;
import com.company.life_simulator.world.WorldBuilder;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;
import java.util.Random;

public class Main{
    private static final Duration PRINT_INTERVAL = Duration.ofSeconds(5);

    public static void main(String[] args) throws IOException, InterruptedException {
        Properties properties = new Properties();
        properties.load(Main.class.getResourceAsStream("/application.properties"));
        properties.stringPropertyNames().stream()
                .filter(name -> System.getProperty(name) == null)
                .forEach(name -> System.setProperty(name, properties.getProperty(name)));

       new Main().simulate();
    }

    public void simulate() throws InterruptedException {
        int seed = new Random().nextInt();
        System.out.println("Seed: " + seed);
        World world = WorldBuilder.createWorld(seed);
        final Instant[] lastPrint = {Instant.now()};
        world.addTickHandler((tick, world1) -> {
            if (lastPrint[0].plus(PRINT_INTERVAL).isBefore(Instant.now())) {
                System.out.println(tick);
                lastPrint[0] = Instant.now();
            }
        });
        while(world.getDwellersCount() > 0)
        {
            world.tick();
        }
        System.out.println(world.getCurrentTick());
    }
}
