package com.company.life_simulator;

import com.company.life_simulator.world.World;
import com.company.life_simulator.world.WorldBuilder;

import java.io.IOException;

public class Main{
    public static void main(String[] args) throws IOException, InterruptedException {
        /*Properties properties = new Properties();
        properties.load(Main.class.getResourceAsStream("/application.properties"));
        properties.stringPropertyNames().stream()
                .filter(name -> System.getProperty(name) == null)
                .forEach(name -> System.setProperty(name, properties.getProperty(name)));
*/
       new Main().simulate();
    }

    public void simulate() throws InterruptedException {
        Thread.sleep(10000);
        World world = WorldBuilder.createWorld();
        while(true)
        {
            world.tick();
        }
    }
}
