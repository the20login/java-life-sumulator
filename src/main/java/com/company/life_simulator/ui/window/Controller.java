package com.company.life_simulator.ui.window;

import com.company.life_simulator.ui.ResizableCanvas;
import com.company.life_simulator.ui.WorldDrawer;
import com.company.life_simulator.world.World;
import com.company.life_simulator.world.WorldPlayer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class Controller {
    @FXML
    public ToggleButton toggleVisibility;
    @FXML
    private ResizableCanvas canvas;
    @FXML
    private Pane pane;
    @FXML
    private Label tickLabel;
    @FXML
    private Label dwellersLabel;
    @FXML
    private ToggleButton playButton;
    @FXML
    private Button resetButton;
    private World world;
    private WorldPlayer player;

    public void setWorldPlayer(WorldPlayer worldPlayer) {
        this.player = worldPlayer;
        this.world = worldPlayer.getWorld();
        redraw();
        world.addTickHandler((tick, world1) -> Platform.runLater(()->{
            if (world.getDwellersCount() == 0)
                playButton.setSelected(false);
            this.onWorldTick();
        }));
    }

    @FXML
    private void initialize() {
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        canvas.setOnResize(canvas1 -> redraw());
        clearCanvas();

        resetButton.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue)
                return;
            player.reset();
            setWorldPlayer(player);
            playButton.setSelected(false);
            onWorldTick();
        });

        playButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (player == null)
                return;
            if (newValue)
                player.play();
            else
                player.stop();
        });

        toggleVisibility.selectedProperty().addListener(observable -> {
            redraw();
        });
    }

    private void onWorldTick()
    {
        tickLabel.setText(String.format("Tick: %d", world.getCurrentTick()));
        dwellersLabel.setText(String.format("Dwellers: %d", world.getDwellersCount()));
        redraw();
    }

    private void redraw()
    {
        if (world != null)
            WorldDrawer.drawWorld(world, canvas, toggleVisibility.isSelected());
        else
            clearCanvas();
    }

    private void clearCanvas()
    {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
}
