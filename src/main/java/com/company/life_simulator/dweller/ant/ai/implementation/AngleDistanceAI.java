package com.company.life_simulator.dweller.ant.ai.implementation;

import com.company.life_simulator.dweller.Dweller;
import com.company.life_simulator.dweller.DwellerType;
import com.company.life_simulator.dweller.Food;
import com.company.life_simulator.dweller.action.Action;
import com.company.life_simulator.dweller.action.ActionBreed;
import com.company.life_simulator.dweller.action.ActionDie;
import com.company.life_simulator.dweller.action.ActionEat;
import com.company.life_simulator.dweller.action.ActionMove;
import com.company.life_simulator.dweller.ant.Ant;
import com.company.life_simulator.dweller.ant.ai.IAntAI;
import com.company.life_simulator.world.World;
import com.company.life_simulator.world.quadtree.Point;
import com.company.life_simulator.world.quadtree.Vector;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AngleDistanceAI implements IAntAI
{
    public static final double DIRECTION_DECISION_ANGLE = Double.valueOf(System.getProperty("dweller.ant.ai.angle_distance.direction_decision_angle", "0.25"));
    public static final double DIRECTION_DECISION_FOOD_COEFFICIENT = Double.valueOf(System.getProperty("dweller.ant.ai.angle_distance.food_coefficient", "4"));
    public static final double DIRECTION_DECISION_ANT_COEFFICIENT = Double.valueOf(System.getProperty("dweller.ant.ai.angle_distance.ant_coefficient", "-2"));

    private Vector speedVector;

    @Override
    public Optional<Action> doAI(Ant self, int tick, World world) {
        if(self.canReproduce(tick))
        {
            return Optional.of(new ActionBreed(self.getId()));
        }
        self.consumeFood();
        if (self.isStarving())
        {
            return Optional.of(new ActionDie(self.getId()));
        }
        List<Dweller> dwellers = world.getDwellersInRange(self.getPosition(), self.getVisibilityRange());
        Optional<Food> nearFoodOptional = dwellers.stream()
                .filter(dweller -> dweller.getType() == DwellerType.food)
                .filter(dweller -> self.getPosition().distance(dweller.getPosition()) <= self.getActionRange())
                .map(dweller -> (Food)dweller)
                .findAny();

        if (nearFoodOptional.isPresent())
        {
            speedVector = null;
            return Optional.of(new ActionEat(self.getId(), nearFoodOptional.get().getId()));
        }

        Optional<Vector> foodOptional = chooseDirection(self.getPosition(), dwellers);

        Point target;
        if (foodOptional.isPresent())
        {
            speedVector = null;
            target = self.calculateMove(self.getPosition().delta(foodOptional.get()));
        }
        else
        {
            if (speedVector == null)
                speedVector = self.getRandomDirection(world.getRandom()).scale(self.getSpeed());
            target = self.getPosition().delta(speedVector);
        }
        return Optional.of(new ActionMove(self.getId(), target));
    }

    public static Optional<Vector> chooseDirection(Point selfPosition, List<Dweller> dwellers)
    {
        long foodCount = dwellers.stream()
                .filter(dweller -> dweller.getType().equals(DwellerType.food))
                .count();

        if (foodCount == 0)
            return Optional.empty();

        List<Triplet<Vector, DwellerType, Double>> vectors = dwellers.stream()
                .map(dweller -> {
                    double coefficient;
                    if (dweller.getType() == DwellerType.ant)
                        coefficient = DIRECTION_DECISION_ANT_COEFFICIENT;
                    else
                        coefficient = DIRECTION_DECISION_FOOD_COEFFICIENT;
                    Vector vector = new Vector(selfPosition, dweller.getPosition());
                    return Triplet.with(vector, dweller.getType(), coefficient / vector.squareLength());
                })
                .collect(Collectors.toList());

        return vectors.stream()
                .filter(pair -> pair.getValue1() == DwellerType.food)
                .map(current -> {
                    double weight = vectors.stream()
                            .map(triplet -> {
                                double angle = Math.abs(triplet.getValue0().angle() - current.getValue0().angle());
                                if (angle > 0.5)
                                    angle -= 0.5;
                                return Pair.with(DIRECTION_DECISION_ANGLE - angle, triplet.getValue2());
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
    }

    @Override
    public String toString() {
        return String.format("[AngleDistanceAI; SpeedVector: %s]", speedVector);
    }
}
