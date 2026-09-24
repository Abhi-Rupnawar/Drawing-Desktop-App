package com.example.drawingapp.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The in-memory drawing: an ordered list of objects plus a format version.
 *
 * <p>The list order is the paint order, so the last object is drawn on top and is also the first
 * candidate during hit testing. The document is intentionally the only mutable aggregate in the
 * model, and it is mutated exclusively through the service layer so that the dirty flag and the
 * repaint notifications stay consistent.
 */
@JsonPropertyOrder({"version", "objects"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class DrawingDocument {

    public static final int CURRENT_VERSION = 1;

    private final int version;
    private final List<DrawingObject> objects;

    public DrawingDocument() {
        this(CURRENT_VERSION, null);
    }

    @JsonCreator
    public DrawingDocument(@JsonProperty("version") int version,
                           @JsonProperty("objects") List<DrawingObject> objects) {
        this.version = version <= 0 ? CURRENT_VERSION : version;
        this.objects = new ArrayList<>();
        if (objects != null) {
            objects.stream().filter(Objects::nonNull).forEach(this.objects::add);
        }
    }

    public int getVersion() {
        return version;
    }

    /** Paint-ordered, unmodifiable view. Mutation goes through {@link #add} / {@link #remove}. */
    public List<DrawingObject> getObjects() {
        return Collections.unmodifiableList(objects);
    }

    public void add(DrawingObject object) {
        objects.add(Objects.requireNonNull(object, "object"));
    }

    public boolean remove(DrawingObject object) {
        return objects.remove(object);
    }

    public int size() {
        return objects.size();
    }
}
