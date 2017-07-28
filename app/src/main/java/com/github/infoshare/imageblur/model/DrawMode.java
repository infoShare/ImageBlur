package com.github.infoshare.imageblur.model;

import static android.graphics.Color.*;

public enum DrawMode {

    Shade(BLACK),
    Red(RED),
    White(WHITE),
    Black(BLACK);

    private int color;

    DrawMode(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
