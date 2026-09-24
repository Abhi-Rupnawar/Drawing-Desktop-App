package com.example.drawingapp.util;

import com.example.drawingapp.model.Bounds;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoundsUtilTest {

    private static final double TOLERANCE = 0.0001;

    @Test
    @DisplayName("All four drag directions produce the same box")
    void normalizesEveryDragDirection() {
        Bounds expected = new Bounds(10, 20, 100, 50);

        assertEquals(expected, BoundsUtil.normalize(10, 20, 110, 70));
        assertEquals(expected, BoundsUtil.normalize(110, 70, 10, 20));
        assertEquals(expected, BoundsUtil.normalize(110, 20, 10, 70));
        assertEquals(expected, BoundsUtil.normalize(10, 70, 110, 20));
    }

    @Test
    void normalizesAZeroSizedDrag() {
        Bounds bounds = BoundsUtil.normalize(5, 5, 5, 5);

        assertEquals(0, bounds.width(), TOLERANCE);
        assertEquals(0, bounds.height(), TOLERANCE);
    }

    @Test
    void measuresPerpendicularDistanceToASegment() {
        assertEquals(10, BoundsUtil.distanceToSegment(50, 10, 0, 0, 100, 0), TOLERANCE);
        assertEquals(0, BoundsUtil.distanceToSegment(50, 0, 0, 0, 100, 0), TOLERANCE);
    }

    @Test
    @DisplayName("Points beyond an end point measure to that end point")
    void clampsDistanceToTheSegmentEnds() {
        assertEquals(20, BoundsUtil.distanceToSegment(120, 0, 0, 0, 100, 0), TOLERANCE);
        assertEquals(10, BoundsUtil.distanceToSegment(-10, 0, 0, 0, 100, 0), TOLERANCE);
    }

    @Test
    void handlesADegenerateSegment() {
        assertEquals(5, BoundsUtil.distanceToSegment(0, 5, 0, 0, 0, 0), TOLERANCE);
    }

    @Test
    void expandsAndContainsPoints() {
        Bounds bounds = new Bounds(0, 0, 10, 10);

        assertTrue(bounds.contains(5, 5));
        assertFalse(bounds.contains(12, 5));
        assertTrue(bounds.expanded(5).contains(12, 5));
        assertEquals(10, bounds.maxX(), TOLERANCE);
    }
}
