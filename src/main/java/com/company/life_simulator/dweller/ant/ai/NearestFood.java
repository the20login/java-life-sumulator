package com.company.life_simulator.dweller.ant.ai;

import com.company.life_simulator.dweller.DwellerType;
import com.company.life_simulator.dweller.Food;
import com.company.life_simulator.dweller.action.*;
import com.company.life_simulator.dweller.ant.Ant;
import com.company.life_simulator.dweller.ant.IAntMemory;
import com.company.life_simulator.world.World;
import com.company.life_simulator.world.quadtree.Point;
import com.company.life_simulator.world.quadtree.Vector;
import org.javatuples.Pair;

import java.util.Optional;

public class NearestFood implements IAntAI {
    @Override
    public IAntMemory createMemory() {
        return new NearestFoodMemory();
    }

    @Override
    public Optional<Action> doAI(Ant self, IAntMemory antMemory, int tick, World world) {
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

        NearestFoodMemory memory = (NearestFoodMemory) antMemory;
        Point target;
        if (foodOptional.isPresent())
        {
            memory.speedVector = null;
            Food food = foodOptional.get();
            if (food.getPosition().squareDistance(self.getPosition()) <= self.getSquareActionRange())
            {
                return Optional.of(new ActionEat(self.getId(), food.getId()));
            }
            target = self.calculateMove(food.getPosition());
        }
        else {
            if (memory.speedVector == null)
                memory.speedVector = self.getRandomDirection(world.getRandom()).scale(self.getSpeed());
            target = self.getPosition().delta(memory.speedVector);
        }
        return Optional.of(new ActionMove(self.getId(), target));
    }

    private static class NearestFoodMemory implements IAntMemory
    {
        Vector speedVector;

        @Override
        public String toString() {
            return String.format("[SpeedVector: %s]", speedVector);
        }
    }
}
