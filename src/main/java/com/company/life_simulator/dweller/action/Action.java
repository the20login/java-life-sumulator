package com.company.life_simulator.dweller.action;

public abstract class Action {
    private final ActionType type;
    private final Integer dwellerId;

    protected Action(Integer id, ActionType type) {
        this.dwellerId = id;
        this.type = type;
    }

    public Integer getDwellerId() {
        return dwellerId;
    }

    public ActionType getType() {
        return type;
    }
}
