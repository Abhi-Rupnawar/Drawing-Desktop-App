package com.example.drawingapp.service;

import com.example.drawingapp.model.DrawingDocument;
import com.example.drawingapp.model.DrawingObject;
import com.example.drawingapp.util.ChangeSupport;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Owns the document that is currently being edited.
 *
 * <p>Every mutation goes through this service, which is what allows the dirty flag and the
 * repaint notification to be maintained in exactly one place instead of being duplicated across
 * the UI. The service has no JavaFX dependency and is therefore directly unit testable.
 */
@Service
public class DrawingService {

    private final ChangeSupport changeSupport = new ChangeSupport();

    private DrawingDocument document = new DrawingDocument();
    private boolean dirty;

    public void addChangeListener(Runnable listener) {
        changeSupport.addListener(listener);
    }

    public DrawingDocument getDocument() {
        return document;
    }

    public List<DrawingObject> getObjects() {
        return document.getObjects();
    }

    public int objectCount() {
        return document.size();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void addObject(DrawingObject object) {
        document.add(Objects.requireNonNull(object, "object"));
        markDirty();
    }

    public void removeObject(DrawingObject object) {
        if (document.remove(object)) {
            markDirty();
        }
    }

    /** Translates an object in world coordinates. */
    public void moveObject(DrawingObject object, double dx, double dy) {
        if (dx == 0 && dy == 0) {
            return;
        }
        object.moveBy(dx, dy);
        markDirty();
    }

    /** Discards the current document and starts an empty one. */
    public void newDocument() {
        replaceDocument(new DrawingDocument());
    }

    /** Installs a document loaded from disk; the result is by definition not dirty. */
    public void replaceDocument(DrawingDocument newDocument) {
        this.document = Objects.requireNonNull(newDocument, "newDocument");
        this.dirty = false;
        changeSupport.fire();
    }

    /** Called after a successful save. */
    public void markSaved() {
        dirty = false;
        changeSupport.fire();
    }

    private void markDirty() {
        dirty = true;
        changeSupport.fire();
    }
}
