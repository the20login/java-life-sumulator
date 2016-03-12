package com.company.life_simulator.dweller.ant;

import com.company.life_simulator.dweller.*;
import com.company.life_simulator.dweller.action.*;
import com.company.life_simulator.world.World;
import com.company.life_simulator.world.quadtree.Point;
import com.company.life_simulator.world.quadtree.Vector;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            speedVector = null;
            return Optional.of(new ActionDie(this.getId()));
        }
        List<Dweller> dwellers = world.getDwellersInRange(getPosition(), getVisibilityRange());
        Optional<Food> foodOptional = dwellers.stream()
                .filter(dweller -> dweller.getType() == DwellerType.food)
                .filter(dweller -> this.getPosition().distance(dweller.getPosition()) <= this.getActionRange())
                .map(dweller -> (Food)dweller)
                .findAny();

        if (foodOptional.isPresent())
        {
            speedVector = null;
            return Optional.of(new ActionEat(this.getId(), foodOptional.get().getId()));
        }

        Optional<Vector> directionVector = chooseDirection(dwellers);

        Point target;
        if (directionVector.isPresent())
        {
            target = calculateMove(this.getPosition().delta(directionVector.get()));
        }
        else
        {
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

    //TODO: optimize
    public Optional<Vector> chooseDirection(List<Dweller> dwellers)
    {
        List<Triplet<Vector, DwellerType, Double>> vectors = dwellers.stream()
                .map(dweller -> {
                    double coefficient;
                    if (dweller.getType() == DwellerType.ant)
                        coefficient = -2;
                    else
                        coefficient = 4;
                    Vector vector = new Vector(getPosition(), dweller.getPosition());
                    return Triplet.with(vector, dweller.getType(), coefficient / vector.squareLength());
                })
                .collect(Collectors.toList());

        Optional<Vector> foodVector = vectors.stream()
                .filter(pair -> pair.getValue1() == DwellerType.food)
                .map(current -> {
                    double weight = vectors.stream()
                            .map(triplet -> {
                                double angle = Math.abs(triplet.getValue0().angle() - current.getValue0().angle());
                                if (angle > 0.5)
                                    angle -= 0.5;
                                return Pair.with(0.25 - angle, triplet.getValue2());
                            })
                            .filter(triplet -> triplet.getValue0() > 0)
                            .map(pair -> pair.getValue0() * pair.getValue1())
                            .mapToDouble(Double::doubleValue)
                            .sum();
                    return Pair.with(current.getValue0(), weight);
                })
                .sorted((o1, o2) -> -Double.compare(o1.getValue1(), o2.getValue1()))
                .map(Pair::getValue0)
                .findFirst();

        if (foodVector.isPresent())
            return foodVector;

        Vector resultVector = vectors.stream()
                .map(Triplet::getValue0)
                .reduce(new Vector(0, 0), Vector::plus)
                .scale(-1);

        if (resultVector.isZeroVector())
            return Optional.empty();

        if (resultVector.squareLength() < getSquareSpeed())
        {
            resultVector = resultVector.scale(this.getSpeed() / resultVector.length());
        }
        return Optional.of(resultVector);
    }
}
