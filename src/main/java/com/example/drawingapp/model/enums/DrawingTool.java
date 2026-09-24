package com.example.drawingapp.model.enums;

/**
 * Tools that can be active in the editor. A tool describes what a primary mouse gesture on the
 * canvas means. It is deliberately separate from {@link ObjectType} because {@code SELECT} does
 * not create an object.
 */
public enum DrawingTool {

    SELECT("Select"),
    RECTANGLE("Rectangle"),
    ELLIPSE("Ellipse"),
    LINE("Line"),
    TEXT("Text");

    private final String displayName;

    DrawingTool(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    /** True when a drag gesture with this tool creates a new object. */
    public boolean isShapeTool() {
        return this == RECTANGLE || this == ELLIPSE || this == LINE;
    }
}
