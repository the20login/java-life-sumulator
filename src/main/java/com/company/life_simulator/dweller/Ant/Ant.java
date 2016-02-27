package com.company.life_simulator.dweller.Ant;

import com.company.life_simulator.dweller.*;
import com.company.life_simulator.world.World;
import com.company.life_simulator.world.quadtree.Point;
import com.company.life_simulator.world.quadtree.Vector;

import java.util.Optional;

public class Ant extends EatingDweller implements IMovingDweller {
    public static final int REPRODUCTION_RATE = Integer.valueOf(System.getProperty("dweller.ant.reproductionRate", "20"));
    public static final double REPRODUCTION_RANGE = Double.valueOf(System.getProperty("dweller.ant.reproductionRange", "20"));
    public static final double VISIBILITY_RANGE = Double.valueOf(System.getProperty("dweller.ant.visibilityRange", "30"));
    public static final double ACTION_RANGE = Double.valueOf(System.getProperty("dweller.ant.action_range", "2"));
    public static final double BASE_SPEED = Double.valueOf(System.getProperty("dweller.ant.baseSpeed", "3"));
    public static final double FOOD_CONSUMPTION = Double.valueOf(System.getProperty("dweller.ant.foodConsumption", "1"));
    public static final double FOOD_SATURATION = Double.valueOf(System.getProperty("dweller.ant.foodSaturation", "50"));

    private Vector speedVector;

    public Ant(Point position, int currentTick, double initialFood) {
        super(DwellerType.ant, position, currentTick, VISIBILITY_RANGE, ACTION_RANGE, BASE_SPEED, REPRODUCTION_RATE, REPRODUCTION_RANGE, initialFood, FOOD_CONSUMPTION, FOOD_SATURATION);
    }

    @Override
    public void doAI(int tick, World world) {
        consumeFood();
        if (isStarving())
        {
            world.removeDweller(this.getPosition());
            return;
        }
        if(canReproduce(tick))
        {
            breed(tick, world);
            return;
        }
        Optional<Food> foodOptional = world.getDwellersInRange(getPosition(), getVisibilityRange())
                .filter(dweller -> dweller.getType().equals(DwellerType.food))
                .map(dweller -> (Food)dweller)
                .findFirst();

        Point target;
        if (foodOptional.isPresent())
        {
            Food food = foodOptional.get();
            if (food.getPosition().squareDistance(this.getPosition()) <= getActionRange())
            {
                world.removeDweller(food.getPosition());
                this.feed(food);
                return;
            }
            target = food.getPosition();
            world.moveDweller(this, calculateMove(target));
        }
        else {
            if (speedVector == null)
                speedVector = getRandomDirection(world.getRandom()).scale(getSpeed());
            world.moveDweller(this, this.getPosition().delta(speedVector));
        }
    }

    @Override
    protected Dweller produceChild(Point position, int tick) {
        return new Ant(position, tick, FOOD_SATURATION / 2);
    }

    @Override
    public double getSquareSpeed() {
        return getSpeed() * getSpeed();
    }
}
