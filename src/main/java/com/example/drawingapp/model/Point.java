package com.example.drawingapp.model;

/**
 * An immutable 2D point. Used to pass coordinate pairs between the view-transform service and
 * the interaction layer. A record is a genuine fit here: the value is small, immutable and has
 * no identity.
 */
public record Point(double x, double y) {

    public Point translate(double dx, double dy) {
        return new Point(x + dx, y + dy);
    }
}
