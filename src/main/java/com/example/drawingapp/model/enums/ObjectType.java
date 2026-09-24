package com.example.drawingapp.model.enums;

/**
 * Discriminator for the supported drawing object types.
 *
 * <p>The constant names are part of the persisted file format: they are used as the Jackson
 * type id, which keeps deserialization restricted to a fixed, known set of classes instead of
 * letting a file decide which Java type to instantiate.
 */
public enum ObjectType {
    RECTANGLE,
    ELLIPSE,
    LINE,
    TEXT
}
