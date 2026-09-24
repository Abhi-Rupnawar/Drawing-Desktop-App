package com.example.drawingapp.model;

import com.example.drawingapp.model.enums.ObjectType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RectangleObjectTest {

    private static final double TOLERANCE = 0.0001;

    @Test
    @DisplayName("A drag from top-left to bottom-right keeps its coordinates")
    void buildsBoundsFromForwardDrag() {
        RectangleObject rectangle = RectangleObject.fromDrag(10, 20, 110, 70);

        Bounds bounds = rectangle.getBounds();
        assertEquals(10, bounds.x(), TOLERANCE);
        assertEquals(20, bounds.y(), TOLERANCE);
        assertEquals(100, bounds.width(), TOLERANCE);
        assertEquals(50, bounds.height(), TOLERANCE);
    }

    @Test
    @DisplayName("A drag in the negative direction is normalized to a positive size")
    void normalizesBackwardDrag() {
        RectangleObject rectangle = RectangleObject.fromDrag(110, 70, 10, 20);

        Bounds bounds = rectangle.getBounds();
        assertEquals(10, bounds.x(), TOLERANCE);
        assertEquals(20, bounds.y(), TOLERANCE);
        assertEquals(100, bounds.width(), TOLERANCE);
        assertEquals(50, bounds.height(), TOLERANCE);
    }

    @Test
    void reportsItsType() {
        assertEquals(ObjectType.RECTANGLE, RectangleObject.fromDrag(0, 0, 1, 1).getType());
    }

    @Test
    void generatesAnIdWhenNoneIsProvided() {
        assertNotNull(RectangleObject.fromDrag(0, 0, 10, 10).getId());
    }

    @Test
    void hitTestsAgainstItsBounds() {
        RectangleObject rectangle = RectangleObject.fromDrag(0, 0, 100, 50);

        assertTrue(rectangle.containsPoint(50, 25, 0));
        assertTrue(rectangle.containsPoint(0, 0, 0));
        assertFalse(rectangle.containsPoint(150, 25, 0));
        assertFalse(rectangle.containsPoint(-1, 25, 0));
    }

    @Test
    void acceptsNearMissesWithinTolerance() {
        RectangleObject rectangle = RectangleObject.fromDrag(0, 0, 100, 50);

        assertFalse(rectangle.containsPoint(103, 25, 0));
        assertTrue(rectangle.containsPoint(103, 25, 5));
    }

    @Test
    @DisplayName("Moving translates the position but not the size")
    void movesByDelta() {
        RectangleObject rectangle = RectangleObject.fromDrag(10, 10, 60, 40);

        rectangle.moveBy(15, -5);

        Bounds bounds = rectangle.getBounds();
        assertEquals(25, bounds.x(), TOLERANCE);
        assertEquals(5, bounds.y(), TOLERANCE);
        assertEquals(50, bounds.width(), TOLERANCE);
        assertEquals(30, bounds.height(), TOLERANCE);
    }
}
