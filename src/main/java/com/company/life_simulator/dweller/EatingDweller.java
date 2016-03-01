package com.company.life_simulator.dweller;

import com.company.life_simulator.world.World;
import com.company.life_simulator.world.quadtree.Point;

public abstract class EatingDweller extends Dweller {
    private final double foodConsumption;
    private final double foodSaturation;
    private double storedFood;


    protected EatingDweller(
            DwellerType type,
            Integer id,
            Point position,
            int currentTick,
            double visibilityRange,
            double actionRange,
            double baseSpeed,
            int reproductionRate,
            double reproductionRange,
            double initialFood,
            double foodConsumption,
            double foodSaturation) {
        super(type, id, position, currentTick, visibilityRange, actionRange, baseSpeed, reproductionRate, reproductionRange);
        this.foodConsumption = foodConsumption;
        this.foodSaturation = foodSaturation;
        this.storedFood = initialFood;
    }

    public void feed(Food food)
    {
        storedFood = Math.max(storedFood + 25, foodSaturation);
    }

    protected void consumeFood()
    {
        storedFood -= foodConsumption;
    }

    protected boolean isStarving()
    {
        return storedFood <= 0;
    }

    protected boolean isSaturated()
    {
        return storedFood >= foodSaturation;
    }

    @Override
    protected boolean canReproduce(int tick) {
        return super.canReproduce(tick) && isSaturated();
    }

    @Override
    public void breed(int tick, World world) {
        storedFood -= foodSaturation / 2;
        super.breed(tick, world);
    }
}
