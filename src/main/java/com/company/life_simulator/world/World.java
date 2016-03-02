package com.company.life_simulator.world;

import com.company.life_simulator.dweller.Dweller;
import com.company.life_simulator.dweller.EatingDweller;
import com.company.life_simulator.dweller.Food;
import com.company.life_simulator.dweller.IMovingDweller;
import com.company.life_simulator.dweller.action.Action;
import com.company.life_simulator.dweller.action.ActionEat;
import com.company.life_simulator.dweller.action.ActionMove;
import com.company.life_simulator.dweller.action.ActionType;
import com.company.life_simulator.world.quadtree.Point;
import com.company.life_simulator.world.quadtree.QuadTree;
import com.company.life_simulator.world.quadtree.Rectangle;
import com.google.common.collect.Lists;
import org.javatuples.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class World {
    private final Rectangle size;
    private final AtomicInteger tickCount = new AtomicInteger(0);
    private final AtomicInteger idGenerator = new AtomicInteger(0);
    private final List<BiConsumer<Integer, World>> handlers = new ArrayList<>();
    private QuadTree<Dweller> quadTree;
    private Map<Integer, Dweller> dwellersMap;
    private final Random random;
    private final EnumMap<ActionType, Consumer<Action>> actionsMap;

    private final ForkJoinPool executor = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

    public World(double width, double height)
    {
        size = new Rectangle(0, 0, width, height);
        quadTree = new QuadTree<>(size);
        dwellersMap = new HashMap<>();
        random = new Random();
        actionsMap = new EnumMap<>(ActionType.class);
        actionsMap.put(ActionType.die, this::processDieAction);
        actionsMap.put(ActionType.eat, this::processEatAction);
        actionsMap.put(ActionType.move, this::processMoveAction);
        actionsMap.put(ActionType.breed, this::processBreedAction);
    }

    public World(double width, double height, int seed)
    {
        size = new Rectangle(0, 0, width, height);
        quadTree = new QuadTree<>(size);
        dwellersMap = new HashMap<>();
        random = new Random(seed);
        actionsMap = new EnumMap<>(ActionType.class);
        actionsMap.put(ActionType.die, this::processDieAction);
        actionsMap.put(ActionType.eat, this::processEatAction);
        actionsMap.put(ActionType.move, this::processMoveAction);
        actionsMap.put(ActionType.breed, this::processBreedAction);
    }

    public Rectangle getSize() {
        return size;
    }

    public void tick()
    {
        int currentTick = tickCount.incrementAndGet();

        List<Action> actions = null;
        try {
            actions = executor.submit(()-> {
                return dwellersMap.values().stream()
                        .parallel()
                        .map(dweller -> dweller.doAI(currentTick, this))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }

        actions.forEach(action -> {
                    Consumer<Action> handler = actionsMap.get(action.getType());
                    handler.accept(action);
                });

        for (BiConsumer<Integer, World> consumer: handlers)
            consumer.accept(currentTick, this);
    }

    private void processDieAction(Action action)
    {
        this.removeDweller(dwellersMap.get(action.getDwellerId()));
    }

    private void processEatAction(Action action)
    {
        ActionEat actionEat = (ActionEat) action;
        EatingDweller dweller = (EatingDweller) dwellersMap.get(actionEat.getDwellerId());
        if (dweller == null)
            return;
        Food food = (Food) dwellersMap.get(actionEat.getFoodId());
        if (food == null)
            return;
        this.removeDweller(food);
        dweller.feed(food);
    }

    private void processMoveAction(Action action) {
        ActionMove actionMove = (ActionMove) action;
        IMovingDweller dweller = (IMovingDweller) dwellersMap.get(actionMove.getDwellerId());
        if (dweller == null)
            return;
        this.moveDweller((Dweller) dweller, actionMove.getTarget());
    }

    private void processBreedAction(Action action) {
        Dweller dweller = dwellersMap.get(action.getDwellerId());
        if (dweller != null)
            dweller.breed(tickCount.get(), this);
    }

    public Integer getNextId() {
        return idGenerator.incrementAndGet();
    }

    public void addDweller(Dweller dweller)
    {
        if (!size.contains(dweller.getPosition()))
        {
            dweller.setPosition(new Point((dweller.getPosition().getX() + size.getWidth()) % size.getWidth(), (dweller.getPosition().getY() + size.getHeight()) % size.getHeight()));
        }
        quadTree.put(dweller.getPosition(), dweller);
        dwellersMap.put(dweller.getId(), dweller);
    }

    public void removeDweller(Dweller dweller)
    {
        quadTree.remove(dweller.getPosition());
        dwellersMap.remove(dweller.getId());
    }

    public void moveDweller(Dweller dweller, Point point)
    {
        if (!size.contains(point))
        {
            point = new Point(point.getX() % size.getWidth(), point.getY() % size.getHeight());
        }
        removeDweller(dweller);
        dweller.setPosition(point);
        addDweller(dweller);
    }

    public void addTickHandler(BiConsumer<Integer, World> handler)
    {
        handlers.add(handler);
    }

    public Stream<Dweller> getDwellers()
    {
        return Lists.newArrayList(dwellersMap.values()).stream();
    }

    public int getDwellersCount()
    {
        return dwellersMap.size();
    }

    public Stream<Dweller> getDwellersInRange(Point point, double range)
    {
        return quadTree.searchWithin(point, range)
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
