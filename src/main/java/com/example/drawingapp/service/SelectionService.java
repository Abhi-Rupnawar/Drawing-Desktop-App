package com.example.drawingapp.service;

import com.example.drawingapp.model.DrawingObject;
import com.example.drawingapp.util.ChangeSupport;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Tracks the selected object and resolves which object sits under a world coordinate.
 *
 * <p>Only single selection is supported, which is all the challenge requires. The selected
 * object is held by reference; because {@link DrawingObject} uses its UUID for equality, a
 * document reload naturally invalidates the reference and the selection is cleared by the
 * controller.
 */
@Service
public class SelectionService {

    private final ChangeSupport changeSupport = new ChangeSupport();

    private DrawingObject selected;

    public void addChangeListener(Runnable listener) {
        changeSupport.addListener(listener);
    }

    public Optional<DrawingObject> getSelected() {
        return Optional.ofNullable(selected);
    }

    public boolean isSelected(DrawingObject object) {
        return selected != null && selected.equals(object);
    }

    /**
     * Selects the topmost object containing the given world point, or clears the selection when
     * the point is empty canvas. Iterating backwards matches the paint order: the object drawn
     * last is the one the user sees on top.
     */
    public Optional<DrawingObject> selectAt(List<DrawingObject> objects,
                                            double worldX, double worldY,
                                            double tolerance) {
        Optional<DrawingObject> hit = findAt(objects, worldX, worldY, tolerance);
        select(hit.orElse(null));
        return hit;
    }

    public Optional<DrawingObject> findAt(List<DrawingObject> objects,
                                          double worldX, double worldY,
                                          double tolerance) {
        for (int i = objects.size() - 1; i >= 0; i--) {
            DrawingObject candidate = objects.get(i);
            if (candidate.containsPoint(worldX, worldY, tolerance)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    public void select(DrawingObject object) {
        if (selected == object) {
            return;
        }
        selected = object;
        changeSupport.fire();
    }

    public void clear() {
        select(null);
    }
}
