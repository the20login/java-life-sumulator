package com.company.life_simulator.dweller.ant.ai;

import com.company.life_simulator.dweller.action.Action;
import com.company.life_simulator.dweller.ant.Ant;
import com.company.life_simulator.dweller.ant.IAntMemory;
import com.company.life_simulator.world.World;

import java.util.Optional;

public interface IAntAI {
    IAntMemory createMemory();

    Optional<Action> doAI(Ant self, IAntMemory antMemory, int tick, World world);
}
