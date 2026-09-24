package com.example.drawingapp.model;

import com.example.drawingapp.model.enums.ObjectType;
import com.example.drawingapp.util.BoundsUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * A straight line segment. The inherited {@code x}/{@code y} is the start point; the end point is
 * stored absolutely, which keeps the JSON readable, so {@link #moveBy} has to translate both.
 */
public class LineObject extends DrawingObject {

    private double endX;
    private double endY;

    @JsonCreator
    public LineObject(@JsonProperty("id") UUID id,
                      @JsonProperty("x") double x,
                      @JsonProperty("y") double y,
                      @JsonProperty("endX") double endX,
                      @JsonProperty("endY") double endY,
                      @JsonProperty("strokeColor") String strokeColor,
                      @JsonProperty("strokeWidth") double strokeWidth) {
        super(id, x, y, strokeColor, strokeWidth);
        this.endX = endX;
        this.endY = endY;
    }

    public static LineObject fromDrag(double startX, double startY, double endX, double endY) {
        return new LineObject(null, startX, startY, endX, endY,
                StyleDefaults.STROKE_COLOR, StyleDefaults.STROKE_WIDTH);
    }

    @Override
    public ObjectType getType() {
        return ObjectType.LINE;
    }

    @Override
    public Bounds getBounds() {
        return BoundsUtil.normalize(getX(), getY(), endX, endY);
    }

    @Override
    public boolean containsPoint(double px, double py, double tolerance) {
        double hitDistance = Math.max(tolerance, getStrokeWidth() / 2);
        return BoundsUtil.distanceToSegment(px, py, getX(), getY(), endX, endY) <= hitDistance;
    }

    @Override
    public void moveBy(double dx, double dy) {
        super.moveBy(dx, dy);
        endX += dx;
        endY += dy;
    }

    public double getEndX() {
        return endX;
    }

    public double getEndY() {
        return endY;
    }
}
