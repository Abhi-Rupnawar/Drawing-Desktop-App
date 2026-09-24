package com.example.drawingapp.model;

import com.example.drawingapp.model.enums.ObjectType;
import com.example.drawingapp.util.BoundsUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * An axis-aligned rectangle. {@code x}/{@code y} is the top-left corner and the size is always
 * stored non-negative, so the rest of the application never has to handle inverted rectangles.
 */
public class RectangleObject extends DrawingObject {

    private final double width;
    private final double height;

    @JsonCreator
    public RectangleObject(@JsonProperty("id") UUID id,
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

    /** Creates a rectangle from the two corners of a drag gesture, in any direction. */
    public static RectangleObject fromDrag(double startX, double startY, double endX, double endY) {
        Bounds bounds = BoundsUtil.normalize(startX, startY, endX, endY);
        return new RectangleObject(null, bounds.x(), bounds.y(), bounds.width(), bounds.height(),
                StyleDefaults.STROKE_COLOR, StyleDefaults.STROKE_WIDTH);
    }

    @Override
    public ObjectType getType() {
        return ObjectType.RECTANGLE;
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(getX(), getY(), width, height);
    }

    @Override
    public boolean containsPoint(double px, double py, double tolerance) {
        return getBounds().expanded(tolerance).contains(px, py);
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
