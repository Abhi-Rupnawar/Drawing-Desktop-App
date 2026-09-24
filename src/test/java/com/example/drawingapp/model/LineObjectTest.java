package com.example.drawingapp.model;

import com.example.drawingapp.model.enums.ObjectType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LineObjectTest {

    private static final double TOLERANCE = 0.0001;

    @Test
    @DisplayName("Bounds are the normalized box around both end points")
    void buildsBoundsFromBothEndPoints() {
        LineObject line = LineObject.fromDrag(100, 80, 20, 10);

        Bounds bounds = line.getBounds();
        assertEquals(20, bounds.x(), TOLERANCE);
        assertEquals(10, bounds.y(), TOLERANCE);
        assertEquals(80, bounds.width(), TOLERANCE);
        assertEquals(70, bounds.height(), TOLERANCE);
    }

    @Test
    void keepsTheDragDirectionInItsEndPoints() {
        LineObject line = LineObject.fromDrag(100, 80, 20, 10);

        assertEquals(100, line.getX(), TOLERANCE);
        assertEquals(80, line.getY(), TOLERANCE);
        assertEquals(20, line.getEndX(), TOLERANCE);
        assertEquals(10, line.getEndY(), TOLERANCE);
    }

    @Test
    void reportsItsType() {
        assertEquals(ObjectType.LINE, LineObject.fromDrag(0, 0, 1, 1).getType());
    }

    @Test
    @DisplayName("Hit testing measures the distance to the segment, not to the bounding box")
    void hitTestsAgainstTheSegment() {
        LineObject line = LineObject.fromDrag(0, 0, 100, 100);

        assertTrue(line.containsPoint(50, 50, 1));
        assertTrue(line.containsPoint(52, 50, 3));
        assertFalse(line.containsPoint(0, 100, 3));
        assertFalse(line.containsPoint(100, 0, 3));
    }

    @Test
    void doesNotExtendBeyondItsEndPoints() {
        LineObject line = LineObject.fromDrag(0, 0, 100, 0);

        assertTrue(line.containsPoint(100, 0, 1));
        assertFalse(line.containsPoint(140, 0, 2));
    }

    @Test
    @DisplayName("Moving translates both end points so the line keeps its shape")
    void movesBothEndPoints() {
        LineObject line = LineObject.fromDrag(0, 0, 100, 50);

        line.moveBy(10, 20);

        assertEquals(10, line.getX(), TOLERANCE);
        assertEquals(20, line.getY(), TOLERANCE);
        assertEquals(110, line.getEndX(), TOLERANCE);
        assertEquals(70, line.getEndY(), TOLERANCE);
    }
}
