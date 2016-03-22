package com.company.life_simulator.dweller.ant.ai;

public enum AntAIType {
    nearestFood(new NearestFood()),
    angleDistance(new AngleDistanceAI());

    private final IAntAI aiImplementation;

    AntAIType(IAntAI aiImplementation)
    {
        this.aiImplementation = aiImplementation;
    }

    public IAntAI getAiImplementation() {
        return aiImplementation;
    }
}
