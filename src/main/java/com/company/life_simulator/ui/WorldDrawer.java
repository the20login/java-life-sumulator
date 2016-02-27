package com.company.life_simulator.ui;

import com.company.life_simulator.dweller.Dweller;
import com.company.life_simulator.dweller.DwellerType;
import com.company.life_simulator.world.World;
import com.company.life_simulator.world.quadtree.Rectangle;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class WorldDrawer {
    private static final Map<DwellerType, Color> COLOR_MAP = new HashMap<>();

    static {
        COLOR_MAP.put(DwellerType.food, Color.GREEN);
        COLOR_MAP.put(DwellerType.ant, Color.BROWN);
    }

    public static void drawWorld(World world, Canvas canvas)
    {
        Rectangle size = world.getSize();
        double ratio = Math.min(canvas.getWidth() / size.getWidth(), canvas.getHeight() / size.getHeight());
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, size.getWidth() * ratio, size.getHeight() * ratio);
        world.getDwellers()
                .forEach(dweller -> drawDweller(gc, dweller, ratio));
    }

    private static void drawDweller(GraphicsContext gc, Dweller dweller, double ratio)
    {
        if (dweller.getVisibilityRange() > 0) {
            gc.setStroke(Color.YELLOW);
            double radius = dweller.getVisibilityRange() * ratio / 2;
            gc.strokeOval(dweller.getPosition().getX() * ratio - radius, dweller.getPosition().getY() * ratio - radius, dweller.getVisibilityRange() * ratio, dweller.getVisibilityRange() * ratio);
        }
        gc.setFill(COLOR_MAP.get(dweller.getType()));
        gc.fillRect(dweller.getPosition().getX() * ratio, dweller.getPosition().getY() * ratio, ratio, ratio);
    }
}
