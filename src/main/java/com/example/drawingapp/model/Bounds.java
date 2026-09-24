package com.example.drawingapp.model;

/**
 * An axis-aligned rectangle in world coordinates, used for hit testing and for drawing the
 * selection indicator. Width and height are always non-negative.
 */
public record Bounds(double x, double y, double width, double height) {

    public Bounds {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Bounds cannot have a negative size");
        }
    }

    public double maxX() {
        return x + width;
    }

    public double maxY() {
        return y + height;
    }

    public boolean contains(double px, double py) {
        return px >= x && px <= maxX() && py >= y && py <= maxY();
    }

    /** Returns a copy grown by {@code margin} on every side; negative margins are ignored. */
    public Bounds expanded(double margin) {
        double safeMargin = Math.max(0, margin);
        return new Bounds(x - safeMargin, y - safeMargin,
                width + 2 * safeMargin, height + 2 * safeMargin);
    }
}
