package com.example.drawingapp.model;

import com.example.drawingapp.model.enums.ObjectType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EllipseObjectTest {

    private static final double TOLERANCE = 0.0001;

    @Test
    void usesTheDragRectangleAsItsBounds() {
        EllipseObject ellipse = EllipseObject.fromDrag(200, 100, 100, 200);

        Bounds bounds = ellipse.getBounds();
        assertEquals(100, bounds.x(), TOLERANCE);
        assertEquals(100, bounds.y(), TOLERANCE);
        assertEquals(100, bounds.width(), TOLERANCE);
        assertEquals(100, bounds.height(), TOLERANCE);
    }

    @Test
    void reportsItsType() {
        assertEquals(ObjectType.ELLIPSE, EllipseObject.fromDrag(0, 0, 1, 1).getType());
    }

    @Test
    @DisplayName("The corner of the bounding box is outside the ellipse")
    void hitTestsAgainstTheEllipseAndNotTheBoundingBox() {
        EllipseObject ellipse = EllipseObject.fromDrag(0, 0, 100, 100);

        assertTrue(ellipse.containsPoint(50, 50, 0));
        assertTrue(ellipse.containsPoint(99, 50, 0));
        assertFalse(ellipse.containsPoint(2, 2, 0));
        assertFalse(ellipse.containsPoint(150, 50, 0));
    }

    @Test
    void supportsNonCircularEllipses() {
        EllipseObject ellipse = EllipseObject.fromDrag(0, 0, 200, 50);

        assertTrue(ellipse.containsPoint(100, 25, 0));
        assertTrue(ellipse.containsPoint(190, 25, 0));
        assertFalse(ellipse.containsPoint(100, 60, 0));
    }

    @Test
    void movesByDelta() {
        EllipseObject ellipse = EllipseObject.fromDrag(0, 0, 100, 100);

        ellipse.moveBy(-20, 30);

        assertEquals(-20, ellipse.getBounds().x(), TOLERANCE);
        assertEquals(30, ellipse.getBounds().y(), TOLERANCE);
        assertTrue(ellipse.containsPoint(30, 80, 0));
    }
}
