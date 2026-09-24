package com.example.drawingapp.service;

import com.example.drawingapp.model.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewTransformServiceTest {

    private static final double TOLERANCE = 0.0001;

    private ViewTransformService viewTransform;

    @BeforeEach
    void setUp() {
        viewTransform = new ViewTransformService();
    }

    @Test
    void startsAtIdentity() {
        assertEquals(1.0, viewTransform.getZoom(), TOLERANCE);
        assertEquals(0, viewTransform.getPanX(), TOLERANCE);
        assertEquals(0, viewTransform.getPanY(), TOLERANCE);

        Point world = viewTransform.toWorld(120, 80);
        assertEquals(120, world.x(), TOLERANCE);
        assertEquals(80, world.y(), TOLERANCE);
    }

    @Test
    @DisplayName("Screen and world conversions are inverses of each other")
    void convertsBothWaysConsistently() {
        viewTransform.zoomBy(2.0, 0, 0);
        viewTransform.panBy(35, -15);

        Point screen = viewTransform.toScreen(200, 150);
        Point world = viewTransform.toWorld(screen.x(), screen.y());

        assertEquals(200, world.x(), TOLERANCE);
        assertEquals(150, world.y(), TOLERANCE);
    }

    @Test
    void appliesZoomAndPanToScreenCoordinates() {
        viewTransform.zoomBy(2.0, 0, 0);
        viewTransform.panBy(50, 20);

        Point screen = viewTransform.toScreen(10, 10);

        assertEquals(70, screen.x(), TOLERANCE);
        assertEquals(40, screen.y(), TOLERANCE);
    }

    @Test
    @DisplayName("Zooming keeps the world point under the anchor in place")
    void zoomsAroundTheAnchorPoint() {
        Point worldUnderCursor = viewTransform.toWorld(400, 300);

        viewTransform.zoomIn(400, 300);
        viewTransform.zoomIn(400, 300);

        Point screenAfterZoom = viewTransform.toScreen(worldUnderCursor.x(), worldUnderCursor.y());
        assertEquals(400, screenAfterZoom.x(), TOLERANCE);
        assertEquals(300, screenAfterZoom.y(), TOLERANCE);
    }

    @Test
    void panningShiftsTheViewWithoutChangingZoom() {
        viewTransform.panBy(100, 50);
        viewTransform.panBy(-30, 10);

        assertEquals(70, viewTransform.getPanX(), TOLERANCE);
        assertEquals(60, viewTransform.getPanY(), TOLERANCE);
        assertEquals(1.0, viewTransform.getZoom(), TOLERANCE);
    }

    @Test
    void clampsZoomToTheSupportedRange() {
        for (int i = 0; i < 50; i++) {
            viewTransform.zoomIn(0, 0);
        }
        assertEquals(ViewTransformService.MAX_ZOOM, viewTransform.getZoom(), TOLERANCE);

        for (int i = 0; i < 100; i++) {
            viewTransform.zoomOut(0, 0);
        }
        assertEquals(ViewTransformService.MIN_ZOOM, viewTransform.getZoom(), TOLERANCE);
    }

    @Test
    void resetRestoresTheDefaultView() {
        viewTransform.zoomIn(100, 100);
        viewTransform.panBy(200, 200);

        viewTransform.reset();

        assertEquals(ViewTransformService.DEFAULT_ZOOM, viewTransform.getZoom(), TOLERANCE);
        assertEquals(0, viewTransform.getPanX(), TOLERANCE);
        assertEquals(0, viewTransform.getPanY(), TOLERANCE);
    }

    @Test
    @DisplayName("A screen distance shrinks in world units as the view zooms in")
    void convertsScreenDistancesToWorldDistances() {
        viewTransform.zoomBy(4.0, 0, 0);

        assertEquals(1.5, viewTransform.toWorldDistance(6), TOLERANCE);
    }

    @Test
    void notifiesListenersWhenTheViewChanges() {
        int[] notifications = {0};
        viewTransform.addChangeListener(() -> notifications[0]++);

        viewTransform.panBy(10, 10);
        viewTransform.zoomIn(0, 0);
        viewTransform.reset();

        assertTrue(notifications[0] >= 3);
    }
}
