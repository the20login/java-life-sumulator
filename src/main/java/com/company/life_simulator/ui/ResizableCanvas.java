package com.company.life_simulator.ui;

import javafx.scene.canvas.Canvas;

import java.util.function.Consumer;

public class ResizableCanvas extends Canvas {

    private Consumer<Canvas> onResize;

    public ResizableCanvas() {
        // Redraw canvas when size changes.
        widthProperty().addListener(evt -> {
            if (onResize != null)
                onResize.accept(this);
        });
        heightProperty().addListener(evt -> {
            if (onResize != null)
                onResize.accept(this);
        });
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    public void setOnResize(Consumer<Canvas> onResize) {
        this.onResize = onResize;
    }

   /* @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }*/
}
