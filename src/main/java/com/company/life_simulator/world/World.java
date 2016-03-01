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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class World {
    private final Rectangle size;
    private final AtomicInteger tickCount = new AtomicInteger(0);
    private final AtomicInteger idGenerator = new AtomicInteger(0);
    private final List<BiConsumer<Integer, World>> handlers = new ArrayList<>();
    private QuadTree<Dweller> quadTree;
    private Map<Integer, Dweller> dwellersMap;
    private final Random random = new Random();

    private final ForkJoinPool executor = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

    public World(double width, double height)
    {
        size = new Rectangle(0, 0, width, height);
        quadTree = new QuadTree<>(size);
        dwellersMap = new HashMap<>();
    }

    public Rectangle getSize() {
        return size;
    }

    public void tick()
    {
        int currentTick = tickCount.incrementAndGet();

        ConcurrentMap<ActionType, ArrayList<Action>> actionsMap = null;
        try {
            actionsMap = executor.submit(()-> {
                return (ConcurrentMap<ActionType, ArrayList<Action>>)dwellersMap.values().stream()
                        .parallel()
                        .map(dweller -> dweller.doAI(currentTick, this))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toConcurrentMap(Action::getType, Lists::newArrayList, (actions, actions2) -> {actions.addAll(actions2); return actions;}));
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }

        if (actionsMap == null)
            return;

        processDieActions(actionsMap.get(ActionType.die));
        processEatActions(actionsMap.get(ActionType.eat));
        processMoveActions(actionsMap.get(ActionType.move));
        processBreedActions(actionsMap.get(ActionType.breed));


        for (BiConsumer<Integer, World> consumer: handlers)
            consumer.accept(currentTick, this);
    }

    private void processDieActions(List<Action> actions)
    {
        if (actions == null)
            return;
        actions.stream()
                .map(Action::getDwellerId)
                .map(dwellersMap::get)
                .forEach(this::removeDweller);
    }

    private void processEatActions(List<Action> actions)
    {
        if (actions == null)
            return;
        actions.stream()
                .map(action -> (ActionEat)action)
                .forEach(actionEat -> {
                    EatingDweller dweller = (EatingDweller)dwellersMap.get(actionEat.getDwellerId());
                    if (dweller == null)
                        return;
                    Food food = (Food)dwellersMap.get(actionEat.getFoodId());
                    if (food == null)
                        return;
                    this.removeDweller(food);
                    dweller.feed(food);
                });
    }

    private void processMoveActions(List<Action> actions) {
        if (actions == null)
            return;
        actions.stream()
                .map(action -> (ActionMove)action)
                .forEach(actionMove -> {
                    IMovingDweller dweller = (IMovingDweller)dwellersMap.get(actionMove.getDwellerId());
                    if (dweller == null)
                        return;
                    this.moveDweller((Dweller)dweller, actionMove.getTarget());
                });
    }

    private void processBreedActions(List<Action> actions) {
        if (actions == null)
            return;
        actions.stream()
                .map(Action::getDwellerId)
                .map(dwellersMap::get)
                .filter(Objects::nonNull)
                .forEach(dweller -> dweller.breed(tickCount.get(), this));
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
