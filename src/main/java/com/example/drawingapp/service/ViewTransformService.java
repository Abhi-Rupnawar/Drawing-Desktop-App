package com.example.drawingapp.service;

import com.example.drawingapp.model.Point;
import com.example.drawingapp.util.ChangeSupport;
import org.springframework.stereotype.Service;

/**
 * The single source of truth for the view transform (zoom plus pan offset).
 *
 * <p>The transform is {@code screen = world * zoom + pan}. Object coordinates are never touched
 * by zooming or panning: the renderer applies the transform to the graphics context, and mouse
 * input is converted back to world coordinates before it reaches the model. That separation is
 * what makes dragging behave identically at every zoom level.
 */
@Service
public class ViewTransformService {

    public static final double MIN_ZOOM = 0.1;
    public static final double MAX_ZOOM = 8.0;
    public static final double DEFAULT_ZOOM = 1.0;

    private static final double ZOOM_STEP = 1.2;

    private final ChangeSupport changeSupport = new ChangeSupport();

    private double zoom = DEFAULT_ZOOM;
    private double panX;
    private double panY;

    public void addChangeListener(Runnable listener) {
        changeSupport.addListener(listener);
    }

    public double getZoom() {
        return zoom;
    }

    public double getPanX() {
        return panX;
    }

    public double getPanY() {
        return panY;
    }

    public Point toWorld(double screenX, double screenY) {
        return new Point(toWorldX(screenX), toWorldY(screenY));
    }

    public double toWorldX(double screenX) {
        return (screenX - panX) / zoom;
    }

    public double toWorldY(double screenY) {
        return (screenY - panY) / zoom;
    }

    public Point toScreen(double worldX, double worldY) {
        return new Point(worldX * zoom + panX, worldY * zoom + panY);
    }

    /** Converts a screen-space distance into a world-space distance. */
    public double toWorldDistance(double screenDistance) {
        return screenDistance / zoom;
    }

    public void panBy(double screenDx, double screenDy) {
        panX += screenDx;
        panY += screenDy;
        changeSupport.fire();
    }

    public void zoomIn(double anchorScreenX, double anchorScreenY) {
        zoomBy(ZOOM_STEP, anchorScreenX, anchorScreenY);
    }

    public void zoomOut(double anchorScreenX, double anchorScreenY) {
        zoomBy(1 / ZOOM_STEP, anchorScreenX, anchorScreenY);
    }

    /**
     * Multiplies the zoom while keeping the world point under the anchor visually fixed, so that
     * wheel zooming feels like it is centred on the cursor.
     */
    public void zoomBy(double factor, double anchorScreenX, double anchorScreenY) {
        double newZoom = clampZoom(zoom * factor);
        if (newZoom == zoom) {
            return;
        }

        double worldX = toWorldX(anchorScreenX);
        double worldY = toWorldY(anchorScreenY);

        zoom = newZoom;
        panX = anchorScreenX - worldX * zoom;
        panY = anchorScreenY - worldY * zoom;

        changeSupport.fire();
    }

    /** Restores 100% zoom and the origin. */
    public void reset() {
        zoom = DEFAULT_ZOOM;
        panX = 0;
        panY = 0;
        changeSupport.fire();
    }

    private static double clampZoom(double value) {
        return Math.min(MAX_ZOOM, Math.max(MIN_ZOOM, value));
    }
}
