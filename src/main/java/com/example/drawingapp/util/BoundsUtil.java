package com.example.drawingapp.util;

import com.example.drawingapp.model.Bounds;

/**
 * Pure geometry helpers. Kept free of any framework dependency so it can be unit tested directly.
 */
public final class BoundsUtil {

    private BoundsUtil() {
    }

    /**
     * Builds a bounding box from two drag points. This is what makes dragging in any direction
     * work: a drag from bottom-right to top-left produces the same box as the opposite drag,
     * with non-negative width and height.
     */
    public static Bounds normalize(double x1, double y1, double x2, double y2) {
        return new Bounds(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
    }

    /** Shortest distance from a point to a line segment. Used for hit testing lines. */
    public static double distanceToSegment(double px, double py,
                                           double x1, double y1,
                                           double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double lengthSquared = dx * dx + dy * dy;

        if (lengthSquared == 0) {
            return Math.hypot(px - x1, py - y1);
        }

        double projection = ((px - x1) * dx + (py - y1) * dy) / lengthSquared;
        double clamped = Math.max(0, Math.min(1, projection));
        double closestX = x1 + clamped * dx;
        double closestY = y1 + clamped * dy;

        return Math.hypot(px - closestX, py - closestY);
    }
}
