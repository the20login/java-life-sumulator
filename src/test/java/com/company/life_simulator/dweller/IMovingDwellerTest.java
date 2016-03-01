package com.company.life_simulator.dweller;

import com.company.life_simulator.dweller.ant.Ant;
import com.company.life_simulator.world.quadtree.Point;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class IMovingDwellerTest {

    @Test
    public void testCalculateMove() throws Exception {
        Ant actor = new Ant(0, new Point(0, 0), 0, 1);
        Point moveRight = actor.calculateMove(new Point(10, 0));
        assertEquals(3, moveRight.getX(), 0);
        assertEquals(0, moveRight.getX(), 0);
    }
}