package com.company.life_simulator.dweller;

import com.company.life_simulator.world.quadtree.Point;
import com.company.life_simulator.world.quadtree.Vector;

import java.util.Random;

public interface IMovingDweller {
    double getSpeed();
    double getSquareSpeed();
    Point getPosition();
    double getActionRange();

    //TODO: optimize
    default Point calculateMove(Point target)
    {
        double squareDistance = this.getPosition().squareDistance(target);
        Vector targetVector = new Vector(this.getPosition(), target);
        Vector speedVector;
        if (squareDistance <= this.getSquareSpeed()) {
            double length = targetVector.length();
            speedVector = targetVector.scale((length - getActionRange() / 2) / length);
        }
        else
            speedVector = targetVector.scale(this.getSpeed()/targetVector.length());
        return this.getPosition().delta(speedVector);
    }

    //TODO: optimize
    default Vector getRandomDirection(Random rand)
    {
        return Vector.getUnitVector(rand.nextDouble());
    }
}
