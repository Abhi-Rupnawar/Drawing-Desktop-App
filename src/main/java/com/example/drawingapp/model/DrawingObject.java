package com.example.drawingapp.model;

import com.example.drawingapp.model.enums.ObjectType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;
import java.util.UUID;

/**
 * Base class for every object that can be placed on the canvas.
 *
 * <p>The model is pure data plus geometry: it has no dependency on JavaFX and knows nothing
 * about how it is rendered. All coordinates are <em>world</em> coordinates and are never
 * modified by zooming or panning.
 *
 * <p>Polymorphic JSON handling uses an explicit {@link JsonSubTypes} whitelist keyed by
 * {@link ObjectType}. Jackson's default typing is deliberately not enabled, so a malicious or
 * corrupt file cannot ask the application to instantiate an arbitrary Java class.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RectangleObject.class, name = "RECTANGLE"),
        @JsonSubTypes.Type(value = EllipseObject.class, name = "ELLIPSE"),
        @JsonSubTypes.Type(value = LineObject.class, name = "LINE"),
        @JsonSubTypes.Type(value = TextObject.class, name = "TEXT")
})
@JsonPropertyOrder({"id", "type", "x", "y"})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class DrawingObject {

    private final UUID id;
    private double x;
    private double y;
    private final String strokeColor;
    private final double strokeWidth;

    protected DrawingObject(UUID id, double x, double y, String strokeColor, double strokeWidth) {
        this.id = id != null ? id : UUID.randomUUID();
        this.x = x;
        this.y = y;
        this.strokeColor = StyleDefaults.strokeColorOrDefault(strokeColor);
        this.strokeWidth = StyleDefaults.strokeWidthOrDefault(strokeWidth);
    }

    public abstract ObjectType getType();

    /** Axis-aligned bounding box in world coordinates. */
    @JsonIgnore
    public abstract Bounds getBounds();

    /**
     * Hit test in world coordinates.
     *
     * @param tolerance extra distance in world units, so that thin shapes stay clickable when
     *                  the view is zoomed out
     */
    public abstract boolean containsPoint(double px, double py, double tolerance);

    /** Moves the object by a world-space delta. Subclasses with extra geometry must extend this. */
    public void moveBy(double dx, double dy) {
        x += dx;
        y += dy;
    }

    public UUID getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getStrokeColor() {
        return strokeColor;
    }

    public double getStrokeWidth() {
        return strokeWidth;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DrawingObject object && id.equals(object.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getType() + "[" + id + "]";
    }
}
