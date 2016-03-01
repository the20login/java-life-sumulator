package com.company.life_simulator.dweller.action;

import com.company.life_simulator.world.quadtree.Point;

public class ActionMove extends Action {
    private final Point target;

    public ActionMove(Integer dwellerId, Point target) {
        super(dwellerId, ActionType.move);
        this.target = target;
    }

    public Point getTarget() {
        return target;
    }
}
