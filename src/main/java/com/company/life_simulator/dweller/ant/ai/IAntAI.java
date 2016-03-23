package com.company.life_simulator.dweller.ant.ai;

import com.company.life_simulator.dweller.action.Action;
import com.company.life_simulator.dweller.ant.Ant;
import com.company.life_simulator.world.World;

import java.util.Optional;

public interface IAntAI {
    Optional<Action> doAI(Ant self, int tick, World world);
}
