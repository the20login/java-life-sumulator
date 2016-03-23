package com.company.life_simulator.dweller.ant;

import com.company.life_simulator.dweller.Dweller;
import com.company.life_simulator.dweller.DwellerType;
import com.company.life_simulator.dweller.EatingDweller;
import com.company.life_simulator.dweller.IMovingDweller;
import com.company.life_simulator.dweller.action.Action;
import com.company.life_simulator.dweller.ant.ai.AntAIType;
import com.company.life_simulator.dweller.ant.ai.IAntAI;
import com.company.life_simulator.world.World;
import com.company.life_simulator.world.quadtree.Point;

import java.util.Optional;

public class Ant extends EatingDweller implements IMovingDweller {
    public static final int REPRODUCTION_RATE = Integer.valueOf(System.getProperty("dweller.ant.reproductionRate", "20"));
    public static final double REPRODUCTION_RANGE = Double.valueOf(System.getProperty("dweller.ant.reproductionRange", "20"));
    public static final double VISIBILITY_RANGE = Double.valueOf(System.getProperty("dweller.ant.visibilityRange", "30"));
    public static final double ACTION_RANGE = Double.valueOf(System.getProperty("dweller.ant.action_range", "2"));
    public static final double BASE_SPEED = Double.valueOf(System.getProperty("dweller.ant.baseSpeed", "3"));
    public static final double FOOD_CONSUMPTION = Double.valueOf(System.getProperty("dweller.ant.foodConsumption", "1"));
    public static final double FOOD_SATURATION = Double.valueOf(System.getProperty("dweller.ant.foodSaturation", "50"));
    public static final AntAIType AI_TYPE = AntAIType.valueOf(System.getProperty("dweller.ant.ai.type", "angleDistance"));

    private final IAntAI aiImplementation;

    public Ant(Integer id, Point position, int currentTick, double initialFood) {
        super(DwellerType.ant, id, position, currentTick, VISIBILITY_RANGE, ACTION_RANGE, BASE_SPEED, REPRODUCTION_RATE, REPRODUCTION_RANGE, initialFood, FOOD_CONSUMPTION, FOOD_SATURATION);
        aiImplementation = AI_TYPE.createAiImplementationInstance();
    }

    @Override
    public Optional<Action> doAI(int tick, World world) {
        return aiImplementation.doAI(this, tick, world);
    }

    @Override
    protected Dweller produceChild(Integer id, Point position, int tick) {
        return new Ant(id, position, tick, FOOD_SATURATION / 2);
    }
}
