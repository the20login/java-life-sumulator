package com.company.life_simulator.world.quadtree;

import org.junit.Test;

import static org.junit.Assert.*;

public class VectorTest {

    @Test
    public void testGetUnitVector() throws Exception {
        Vector vector = Vector.getUnitVector(0);
        assertEquals(vector.getX(), 1, 0.000001);
        assertEquals(vector.getY(), 0, 0.000001);

        vector = Vector.getUnitVector(0.25);
        assertEquals(vector.getX(), 0, 0.000001);
        assertEquals(vector.getY(), 1, 0.000001);

        vector = Vector.getUnitVector(0.5);
        assertEquals(vector.getX(), -1, 0.000001);
        assertEquals(vector.getY(), 0, 0.000001);

        vector = Vector.getUnitVector(0.75);
        assertEquals(vector.getX(), 0, 0.000001);
        assertEquals(vector.getY(), -1, 0.000001);

        vector = Vector.getUnitVector(1);
        assertEquals(vector.getX(), 1, 0.000001);
        assertEquals(vector.getY(), 0, 0.000001);
    }
}