package com.example.drawingapp.service;

import com.example.drawingapp.model.DrawingObject;
import com.example.drawingapp.model.RectangleObject;
import com.example.drawingapp.model.TextObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectionServiceTest {

    private SelectionService selectionService;

    @BeforeEach
    void setUp() {
        selectionService = new SelectionService();
    }

    @Test
    void selectsTheObjectUnderThePoint() {
        RectangleObject rectangle = RectangleObject.fromDrag(0, 0, 100, 100);

        selectionService.selectAt(List.of(rectangle), 50, 50, 0);

        assertTrue(selectionService.getSelected().isPresent());
        assertSame(rectangle, selectionService.getSelected().orElseThrow());
        assertTrue(selectionService.isSelected(rectangle));
    }

    @Test
    @DisplayName("Clicking empty canvas clears the selection")
    void clearsSelectionOnEmptyArea() {
        RectangleObject rectangle = RectangleObject.fromDrag(0, 0, 100, 100);
        selectionService.select(rectangle);

        selectionService.selectAt(List.of(rectangle), 500, 500, 0);

        assertTrue(selectionService.getSelected().isEmpty());
    }

    @Test
    @DisplayName("Overlapping objects resolve to the one drawn last")
    void selectsTheTopmostObject() {
        RectangleObject below = RectangleObject.fromDrag(0, 0, 100, 100);
        RectangleObject above = RectangleObject.fromDrag(20, 20, 80, 80);

        selectionService.selectAt(List.of(below, above), 50, 50, 0);

        assertSame(above, selectionService.getSelected().orElseThrow());
    }

    @Test
    void findingDoesNotChangeTheSelection() {
        RectangleObject rectangle = RectangleObject.fromDrag(0, 0, 100, 100);

        assertTrue(selectionService.findAt(List.of(rectangle), 50, 50, 0).isPresent());
        assertTrue(selectionService.getSelected().isEmpty());
    }

    @Test
    void appliesTheToleranceWhenHitTesting() {
        RectangleObject rectangle = RectangleObject.fromDrag(0, 0, 100, 100);
        List<DrawingObject> objects = List.of(rectangle);

        assertTrue(selectionService.findAt(objects, 104, 50, 0).isEmpty());
        assertTrue(selectionService.findAt(objects, 104, 50, 6).isPresent());
    }

    @Test
    void returnsEmptyForAnEmptyDocument() {
        assertTrue(selectionService.selectAt(List.of(), 10, 10, 2).isEmpty());
    }

    @Test
    void notifiesListenersOnlyWhenTheSelectionActuallyChanges() {
        TextObject text = TextObject.at(0, 0, "hello");
        int[] notifications = {0};
        selectionService.addChangeListener(() -> notifications[0]++);

        selectionService.select(text);
        selectionService.select(text);
        selectionService.clear();

        assertEquals(2, notifications[0]);
        assertFalse(selectionService.isSelected(text));
    }
}
