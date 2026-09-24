package com.example.drawingapp.model;

import com.example.drawingapp.model.enums.ObjectType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * A single line of text anchored at its top-left corner.
 *
 * <p>The bounding box is estimated from the font size and the character count instead of being
 * measured with JavaFX font metrics. This keeps the model free of UI dependencies and unit
 * testable; the cost is a slightly approximate selection box, which is acceptable here because
 * resize handles are out of scope.
 */
public class TextObject extends DrawingObject {

    private static final double AVERAGE_CHAR_WIDTH_RATIO = 0.58;
    private static final double LINE_HEIGHT_RATIO = 1.25;

    private final String text;
    private final double fontSize;

    @JsonCreator
    public TextObject(@JsonProperty("id") UUID id,
                      @JsonProperty("x") double x,
                      @JsonProperty("y") double y,
                      @JsonProperty("text") String text,
                      @JsonProperty("fontSize") double fontSize,
                      @JsonProperty("strokeColor") String strokeColor,
                      @JsonProperty("strokeWidth") double strokeWidth) {
        super(id, x, y, strokeColor, strokeWidth);
        this.text = text == null ? "" : text;
        this.fontSize = StyleDefaults.fontSizeOrDefault(fontSize);
    }

    public static TextObject at(double x, double y, String text) {
        return new TextObject(null, x, y, text, StyleDefaults.FONT_SIZE,
                StyleDefaults.STROKE_COLOR, StyleDefaults.STROKE_WIDTH);
    }

    @Override
    public ObjectType getType() {
        return ObjectType.TEXT;
    }

    @Override
    public Bounds getBounds() {
        double width = Math.max(1, text.length()) * fontSize * AVERAGE_CHAR_WIDTH_RATIO;
        return new Bounds(getX(), getY(), width, fontSize * LINE_HEIGHT_RATIO);
    }

    @Override
    public boolean containsPoint(double px, double py, double tolerance) {
        return getBounds().expanded(tolerance).contains(px, py);
    }

    public String getText() {
        return text;
    }

    public double getFontSize() {
        return fontSize;
    }
}
