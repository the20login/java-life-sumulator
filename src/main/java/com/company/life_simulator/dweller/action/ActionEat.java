package com.company.life_simulator.dweller.action;

public class ActionEat extends Action {
    private final Integer foodId;

    public ActionEat(Integer dwellerId, Integer foodId) {
        super(dwellerId, ActionType.eat);
        this.foodId = foodId;
    }

    public Integer getFoodId() {
        return foodId;
    }
}
