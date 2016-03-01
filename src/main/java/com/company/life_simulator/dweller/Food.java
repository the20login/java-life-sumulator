package com.company.life_simulator.dweller;

import com.company.life_simulator.dweller.action.Action;
import com.company.life_simulator.dweller.action.ActionBreed;
import com.company.life_simulator.world.World;
import com.company.life_simulator.world.quadtree.Point;

import java.util.Optional;

public class Food extends Dweller{
    public static final int REPRODUCTION_RATE = Integer.valueOf(System.getProperty("dweller.food.reproductionRate", "30"));
    public static final double REPRODUCTION_RANGE = Double.valueOf(System.getProperty("dweller.food.reproductionRange", "30"));

    public Food(Integer id, Point position, int currentTick) {
        super(DwellerType.food, id, position, currentTick, 0, 0, 0, REPRODUCTION_RATE, REPRODUCTION_RANGE);
    }

    @Override
    public Optional<Action> doAI(int tick, World world) {
        if(canReproduce(tick))
        {
            return Optional.of(new ActionBreed(this.getId()));
        }
        return Optional.empty();
    }

    @Override
    protected Dweller produceChild(Integer id, Point position, int tick) {
        return new Food(id, position, tick);
    }


}
