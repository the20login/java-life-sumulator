package com.company.life_simulator.world;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WorldPlayer {
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat("WorldPlayerWorker-%d").build());
    private World world;
    private final long delay;
    private final TimeUnit unit;
    private volatile boolean played = false;
    private Future nextTick;

    public WorldPlayer(long delay, TimeUnit unit) {
        this.world = createWorld();
        this.delay = delay;
        this.unit = unit;
    }

    public synchronized void play()
    {
        played = true;
        nextTick = executor.submit(this::callTick);
    }

    public synchronized void stop()
    {
        played = false;
        if (nextTick != null) {
            nextTick.cancel(false);
            nextTick = null;
        }
    }

    public void reset()
    {
        stop();
        world = createWorld();
    }

    public World getWorld()
    {
        return world;
    }

    private World createWorld()
    {
        return WorldBuilder.createWorld();
    }

    private void callTick()
    {
        nextTick = null;
        try {
            world.tick();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (played)
            nextTick = executor.schedule(this::callTick, delay, unit);
    }

    public void destroy()
    {
        stop();
        executor.shutdown();
    }
}
