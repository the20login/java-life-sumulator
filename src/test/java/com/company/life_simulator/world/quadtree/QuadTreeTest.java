package com.company.life_simulator.world.quadtree;

import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class QuadTreeTest {
    private QuadTree<Integer> quadTree;

    @Before
    public void init()
    {
        quadTree = new QuadTree<>(new Rectangle(0, 0, 100, 100));
        int count = 0;
        quadTree.put(new Point(1, 1), count++);
        quadTree.put(new Point(100, 100), count++);
        quadTree.put(new Point(2, 2), count++);
        quadTree.put(new Point(1, 90), count++);
        quadTree.put(new Point(50, 50), count++);
        quadTree.put(new Point(8, 54), count++);
        quadTree.put(new Point(55, 45), count++);
        quadTree.put(new Point(45, 55), count++);
    }

    @Test
    public void testSearchWithinRectangle() throws Exception {
        Set<Integer> set = quadTree.searchWithin(new Rectangle(0, 0, 10, 10)).collect(Collectors.toSet());

        assertEquals(2, set.size());
        assertTrue(set.contains(0));
        assertTrue(set.contains(2));
    }

    @Test
    public void testSearchWithinCircle() throws Exception {
        Set<Integer> set = quadTree.searchWithin(new Point(50, 50), 7.5).collect(Collectors.toSet());

        assertEquals(3, set.size());
        assertTrue(set.contains(4));
        assertTrue(set.contains(6));
        assertTrue(set.contains(7));
    }
}