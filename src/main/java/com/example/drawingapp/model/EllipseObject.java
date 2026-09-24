package com.example.drawingapp.model;

import com.example.drawingapp.model.enums.ObjectType;
import com.example.drawingapp.util.BoundsUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * An ellipse described by its bounding box, which also covers circles (equal width and height).
 * Storing the bounding box instead of a centre and two radii keeps the geometry consistent with
 * {@link RectangleObject} and makes the drag gesture trivially the same.
 */
public class EllipseObject extends DrawingObject {

    private final double width;
    private final double height;

    @JsonCreator
    public EllipseObject(@JsonProperty("id") UUID id,
                         @JsonProperty("x") double x,
                         @JsonProperty("y") double y,
                         @JsonProperty("width") double width,
                         @JsonProperty("height") double height,
                         @JsonProperty("strokeColor") String strokeColor,
                         @JsonProperty("strokeWidth") double strokeWidth) {
        super(id, x, y, strokeColor, strokeWidth);
        this.width = Math.max(0, width);
        this.height = Math.max(0, height);
    }

    /** Creates an ellipse from the two corners of a drag gesture, in any direction. */
    public static EllipseObject fromDrag(double startX, double startY, double endX, double endY) {
        Bounds bounds = BoundsUtil.normalize(startX, startY, endX, endY);
        return new EllipseObject(null, bounds.x(), bounds.y(), bounds.width(), bounds.height(),
                StyleDefaults.STROKE_COLOR, StyleDefaults.STROKE_WIDTH);
    }

    @Override
    public ObjectType getType() {
        return ObjectType.ELLIPSE;
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(getX(), getY(), width, height);
    }

    @Override
    public boolean containsPoint(double px, double py, double tolerance) {
        double radiusX = width / 2 + tolerance;
        double radiusY = height / 2 + tolerance;
        if (radiusX <= 0 || radiusY <= 0) {
            return false;
        }

        double normalizedX = (px - (getX() + width / 2)) / radiusX;
        double normalizedY = (py - (getY() + height / 2)) / radiusY;

        return normalizedX * normalizedX + normalizedY * normalizedY <= 1.0;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
