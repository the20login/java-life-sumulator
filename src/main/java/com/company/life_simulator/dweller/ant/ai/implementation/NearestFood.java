package com.company.life_simulator.dweller.ant.ai.implementation;

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

import java.util.Optional;

public class NearestFood implements IAntAI
{
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
        Optional<Food> foodOptional = world.getDwellersInRange(self.getPosition(), self.getVisibilityRange()).stream()
                .filter(dweller -> dweller.getType().equals(DwellerType.food))
                .map(food -> org.javatuples.Pair.with(self.getPosition().distance(food.getPosition()), (Food)food))
                .sorted((pair1, pair2) -> Double.compare(pair1.getValue0(), pair2.getValue0()))
                .map(Pair::getValue1)
                .findFirst();

        Point target;
        if (foodOptional.isPresent())
        {
            speedVector = null;
            Food food = foodOptional.get();
            if (food.getPosition().squareDistance(self.getPosition()) <= self.getSquareActionRange())
            {
                return Optional.of(new ActionEat(self.getId(), food.getId()));
            }
            target = self.calculateMove(food.getPosition());
        }
        else {
            if (speedVector == null)
                speedVector = self.getRandomDirection(world.getRandom()).scale(self.getSpeed());
            target = self.getPosition().delta(speedVector);
        }
        return Optional.of(new ActionMove(self.getId(), target));
    }

    @Override
    public String toString() {
        return String.format("[NearestFoodAI; SpeedVector: %s]", speedVector);
    }
}
