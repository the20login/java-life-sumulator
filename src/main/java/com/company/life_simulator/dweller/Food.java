package com.company.life_simulator.dweller;

import com.company.life_simulator.world.World;
import com.company.life_simulator.world.quadtree.Point;

public class Food extends Dweller{
    public static final int REPRODUCTION_RATE = Integer.valueOf(System.getProperty("dweller.food.reproductionRate", "30"));
    public static final double REPRODUCTION_RANGE = Double.valueOf(System.getProperty("dweller.food.reproductionRange", "30"));

    public Food(Point position, int currentTick) {
        super(DwellerType.food, position, currentTick, 0, 0, 0, REPRODUCTION_RATE, REPRODUCTION_RANGE);
    }

    @Override
    public void doAI(int tick, World world) {
        if(canReproduce(tick))
        {
            breed(tick, world);
        }
    }

    @Override
    protected Dweller produceChild(Point position, int tick) {
        return new Food(position, tick);
    }


}
