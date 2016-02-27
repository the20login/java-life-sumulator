package com.company.life_simulator.world;

import com.company.life_simulator.dweller.Dweller;
import com.company.life_simulator.world.quadtree.Point;
import com.company.life_simulator.world.quadtree.QuadTree;
import com.company.life_simulator.world.quadtree.Rectangle;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class World {
    private final Rectangle size;
    private final AtomicInteger tickCount = new AtomicInteger(0);
    private final List<BiConsumer<Integer, World>> handlers = new ArrayList<>();
    private QuadTree<Dweller> dwellers;
    private final Random random = new Random();

    public World(double width, double height)
    {
        size = new Rectangle(0, 0, width, height);
        dwellers = new QuadTree<>(size);
    }

    public Rectangle getSize() {
        return size;
    }

    public void tick()
    {
        int currentTick = tickCount.incrementAndGet();

        /*for (Dweller dweller: new ArrayList<>(dwellers))
        {
            dweller.doAI(currentTick, this);
        }*/
        dwellers.getValues()
                .collect(Collectors.toList())
                .forEach(dweller -> dweller.doAI(currentTick, this));
        for (BiConsumer<Integer, World> consumer: handlers)
            consumer.accept(currentTick, this);
    }

    public void addDweller(Dweller dweller)
    {
        if (!size.isContains(dweller.getPosition()))
        {

            dweller.setPosition(new Point((dweller.getPosition().getX() + size.getWidth()) % size.getWidth(), (dweller.getPosition().getY() + size.getWidth()) % size.getHeight()));
        }
        dwellers.put(dweller.getPosition(), dweller);
    }

    public void removeDweller(Point position)
    {
        dwellers.remove(position);
    }

    public void moveDweller(Dweller dweller, Point point)
    {
        if (!size.isContains(point))
        {
            point = new Point(point.getX() % size.getWidth(), point.getY() % size.getHeight());
        }
        removeDweller(dweller.getPosition());
        dweller.setPosition(point);
        addDweller(dweller);
    }

    public void addTickHandler(BiConsumer<Integer, World> handler)
    {
        handlers.add(handler);
    }

    public Stream<Dweller> getDwellers()
    {
        return dwellers.getValues();
    }

    public int getDwellersCount()
    {
        return dwellers.getCount();
    }

    public Stream<Dweller> getDwellersInRange(Point point, double range)
    {
        return dwellers.searchWithin(point, range)
                .filter(dweller -> !dweller.getPosition().equals(point))
                .map(dweller -> Pair.with(dweller, point.squareDistance(dweller.getPosition())))
                .sorted((o1, o2) -> o1.getValue1().compareTo(o2.getValue1()))
                .map(Pair::getValue0);
    }

    public int getCurrentTick()
    {
        return tickCount.get();
    }

    public Random getRandom() {
        return random;
    }
}
