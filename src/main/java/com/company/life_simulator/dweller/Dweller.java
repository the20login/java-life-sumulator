package com.company.life_simulator.dweller;

import com.company.life_simulator.dweller.action.Action;
import com.company.life_simulator.world.World;
import com.company.life_simulator.world.quadtree.Point;
import com.company.life_simulator.world.quadtree.Vector;

import java.util.Optional;

public abstract class Dweller {
    private final DwellerType type;
    private final Integer id;
    private final int birthTick;
    private final double visibilityRange;
    private final double actionRange;
    private final double baseSpeed;
    private final int reproductionRate;
    private final double reproductionRange;

    private Point position;
    private int lastReproduction;

    protected Dweller(DwellerType type,
                      Integer id,
                      Point position,
                      int currentTick,
                      double visibilityRange,
                      double actionRange,
                      double baseSpeed,
                      int reproductionRate,
                      double reproductionRange)
    {
        this.type = type;
        this.id = id;
        this.position = position;
        birthTick = currentTick;

        this.visibilityRange = visibilityRange;
        this.actionRange = actionRange;
        this.baseSpeed = baseSpeed;

        this.reproductionRate = reproductionRate;
        this.reproductionRange = reproductionRange;
        lastReproduction = currentTick;
    }

    public Integer getId() {
        return id;
    }

    public int getBirthTick()
    {
        return birthTick;
    }

    public DwellerType getType() {
        return type;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position)
    {
        this.position = position;
    }

    public double getVisibilityRange()
    {
        return visibilityRange;
    }

    public double getActionRange()
    {
        return actionRange;
    }

    public double getSquareActionRange()
    {
        return actionRange * actionRange;
    }

    public double getSpeed() {
        return baseSpeed;
    }

    public double getBaseSpeed() {
        return baseSpeed;
    }

    public int getReproductionRate() {
        return reproductionRate;
    }

    public double getReproductionRange() {
        return reproductionRange;
    }

    public abstract Optional<Action> doAI(int tick, World world);

    protected abstract Dweller produceChild(Integer id, Point position, int tick);

    protected boolean canReproduce(int tick)
    {
        return tick - lastReproduction >= reproductionRate;
    }

    public void breed(int tick, World world)
    {
        Vector childVector = Vector.getUnitVector(world.getRandom().nextDouble() * Math.PI * 2)
                .scale(world.getRandom().nextDouble() * getReproductionRange());
        Point childPoint = this.getPosition().delta(childVector);
        world.addDweller(produceChild(world.getNextId(), childPoint, tick));
        lastReproduction = tick;
    }

    @Override
    public String toString() {
        return String.format("[%s, %s]", getPosition(), getType());
    }
}
