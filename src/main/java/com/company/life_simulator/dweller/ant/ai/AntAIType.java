package com.company.life_simulator.dweller.ant.ai;

import com.company.life_simulator.dweller.ant.ai.implementation.AngleDistanceAI;
import com.company.life_simulator.dweller.ant.ai.implementation.NearestFood;

import java.util.function.Supplier;

public enum AntAIType {
    nearestFood(NearestFood::new),
    angleDistance(AngleDistanceAI::new);

    private final Supplier<IAntAI> aiImplementation;

    AntAIType(Supplier<IAntAI> aiImplementation)
    {
        this.aiImplementation = aiImplementation;
    }

    public IAntAI createAiImplementationInstance() {
        return aiImplementation.get();
    }
}
