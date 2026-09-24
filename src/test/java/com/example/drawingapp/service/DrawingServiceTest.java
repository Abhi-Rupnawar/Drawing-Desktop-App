package com.example.drawingapp.service;

import com.example.drawingapp.model.DrawingDocument;
import com.example.drawingapp.model.LineObject;
import com.example.drawingapp.model.RectangleObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DrawingServiceTest {

    private static final double TOLERANCE = 0.0001;

    private DrawingService drawingService;

    @BeforeEach
    void setUp() {
        drawingService = new DrawingService();
    }

    @Test
    void startsWithAnEmptyCleanDocument() {
        assertEquals(0, drawingService.objectCount());
        assertFalse(drawingService.isDirty());
    }

    @Test
    @DisplayName("Adding an object marks the drawing as having unsaved changes")
    void addingAnObjectMarksTheDocumentDirty() {
        drawingService.addObject(RectangleObject.fromDrag(0, 0, 10, 10));

        assertEquals(1, drawingService.objectCount());
        assertTrue(drawingService.isDirty());
    }

    @Test
    void savingClearsTheDirtyFlag() {
        drawingService.addObject(RectangleObject.fromDrag(0, 0, 10, 10));

        drawingService.markSaved();

        assertFalse(drawingService.isDirty());
    }

    @Test
    @DisplayName("Moving an object updates its world coordinates")
    void movesObjectsInWorldCoordinates() {
        RectangleObject rectangle = RectangleObject.fromDrag(10, 10, 60, 60);
        drawingService.addObject(rectangle);
        drawingService.markSaved();

        drawingService.moveObject(rectangle, 25, -5);

        assertEquals(35, rectangle.getX(), TOLERANCE);
        assertEquals(5, rectangle.getY(), TOLERANCE);
        assertTrue(drawingService.isDirty());
    }

    @Test
    void aZeroMoveDoesNotDirtyTheDocument() {
        RectangleObject rectangle = RectangleObject.fromDrag(0, 0, 10, 10);
        drawingService.addObject(rectangle);
        drawingService.markSaved();

        drawingService.moveObject(rectangle, 0, 0);

        assertFalse(drawingService.isDirty());
    }

    @Test
    void removesObjects() {
        LineObject line = LineObject.fromDrag(0, 0, 10, 10);
        drawingService.addObject(line);
        drawingService.markSaved();

        drawingService.removeObject(line);

        assertEquals(0, drawingService.objectCount());
        assertTrue(drawingService.isDirty());
    }

    @Test
    void newDocumentDiscardsEverything() {
        drawingService.addObject(RectangleObject.fromDrag(0, 0, 10, 10));

        drawingService.newDocument();

        assertEquals(0, drawingService.objectCount());
        assertFalse(drawingService.isDirty());
    }

    @Test
    @DisplayName("A document loaded from disk is not dirty")
    void replacingTheDocumentResetsTheDirtyFlag() {
        drawingService.addObject(RectangleObject.fromDrag(0, 0, 10, 10));

        drawingService.replaceDocument(new DrawingDocument(DrawingDocument.CURRENT_VERSION,
                List.of(LineObject.fromDrag(0, 0, 5, 5))));

        assertEquals(1, drawingService.objectCount());
        assertFalse(drawingService.isDirty());
    }

    @Test
    void notifiesListenersOnEveryChange() {
        int[] notifications = {0};
        drawingService.addChangeListener(() -> notifications[0]++);

        drawingService.addObject(RectangleObject.fromDrag(0, 0, 10, 10));
        drawingService.markSaved();

        assertEquals(2, notifications[0]);
    }
}
