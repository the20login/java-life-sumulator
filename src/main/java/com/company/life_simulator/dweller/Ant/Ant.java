package com.company.life_simulator.dweller.ant;

import com.company.life_simulator.dweller.*;
import com.company.life_simulator.dweller.action.*;
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

    public Ant(Integer id, Point position, int currentTick, double initialFood) {
        super(DwellerType.ant, id, position, currentTick, VISIBILITY_RANGE, ACTION_RANGE, BASE_SPEED, REPRODUCTION_RATE, REPRODUCTION_RANGE, initialFood, FOOD_CONSUMPTION, FOOD_SATURATION);
    }

    @Override
    public Optional<Action> doAI(int tick, World world) {
        if(canReproduce(tick))
        {
            return Optional.of(new ActionBreed(this.getId()));
        }
        consumeFood();
        if (isStarving())
        {
            return Optional.of(new ActionDie(this.getId()));
        }
        Optional<Food> foodOptional = world.getDwellersInRange(getPosition(), getVisibilityRange())
                .filter(dweller -> dweller.getType().equals(DwellerType.food))
                .map(dweller -> (Food)dweller)
                .findFirst();

        Point target;
        if (foodOptional.isPresent())
        {
            Food food = foodOptional.get();
            if (food.getPosition().squareDistance(this.getPosition()) <= getSquareActionRange())
            {
                return Optional.of(new ActionEat(this.getId(), food.getId()));
            }
            target = calculateMove(food.getPosition());
        }
        else {
            if (speedVector == null)
                speedVector = getRandomDirection(world.getRandom()).scale(getSpeed());
            target = this.getPosition().delta(speedVector);
        }
        return Optional.of(new ActionMove(this.getId(), target));
    }

    @Override
    protected Dweller produceChild(Integer id, Point position, int tick) {
        return new Ant(id, position, tick, FOOD_SATURATION / 2);
    }

    @Override
    public double getSquareSpeed() {
        return getSpeed() * getSpeed();
    }
}
