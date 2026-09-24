package com.example.drawingapp.ui;

import com.example.drawingapp.controller.DrawingController;
import com.example.drawingapp.service.DrawingService;
import com.example.drawingapp.service.SelectionService;
import com.example.drawingapp.service.ViewTransformService;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import org.springframework.stereotype.Component;

/**
 * The drawing surface.
 *
 * <p>A {@link Canvas} has a fixed size, so it is wrapped in a resizable {@link Pane} and its
 * size is bound to the pane. The class is a thin adapter: it turns JavaFX events into calls on
 * {@link DrawingController} and repaints. No interaction rules live here, which is why the
 * controller can be tested without a JavaFX toolkit.
 *
 * <p>Panning is bound to <em>middle mouse drag</em> or <em>space + left drag</em>; the second
 * option exists because many laptop trackpads have no middle button.
 */
@Component
public class DrawingCanvas extends Pane {

    private static final double WHEEL_ZOOM_STEP = 1.1;

    private final Canvas canvas = new Canvas();
    private final CanvasRenderer renderer;
    private final DrawingController controller;
    private final DrawingService drawingService;
    private final SelectionService selectionService;

    private boolean spacePanning;
    private boolean panGestureActive;
    private boolean primaryGestureActive;
    private double lastPanScreenX;
    private double lastPanScreenY;

    public DrawingCanvas(CanvasRenderer renderer,
                         DrawingController controller,
                         DrawingService drawingService,
                         SelectionService selectionService,
                         ViewTransformService viewTransform) {
        this.renderer = renderer;
        this.controller = controller;
        this.drawingService = drawingService;
        this.selectionService = selectionService;

        getChildren().add(canvas);
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> redraw());
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> redraw());

        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);
        canvas.setOnScroll(this::handleScroll);

        drawingService.addChangeListener(this::redraw);
        selectionService.addChangeListener(this::redraw);
        viewTransform.addChangeListener(this::redraw);
    }

    /** Enabled while the space bar is held, so a left drag pans instead of drawing. */
    public void setSpacePanning(boolean spacePanning) {
        this.spacePanning = spacePanning;
        setCursor(spacePanning ? Cursor.OPEN_HAND : Cursor.DEFAULT);
    }

    public double centerX() {
        return canvas.getWidth() / 2;
    }

    public double centerY() {
        return canvas.getHeight() / 2;
    }

    public void redraw() {
        renderer.render(canvas.getGraphicsContext2D(), canvas.getWidth(), canvas.getHeight(),
                drawingService.getObjects(),
                controller.getPreviewObject().orElse(null),
                selectionService.getSelected().orElse(null));
    }

    private void handleMousePressed(MouseEvent event) {
        if (event.getButton() == MouseButton.MIDDLE || (spacePanning && event.isPrimaryButtonDown())) {
            startPanGesture(event);
        } else if (event.getButton() == MouseButton.PRIMARY) {
            primaryGestureActive = true;
            controller.onPrimaryPressed(event.getX(), event.getY());
            redraw();
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        if (panGestureActive) {
            controller.panBy(event.getX() - lastPanScreenX, event.getY() - lastPanScreenY);
            lastPanScreenX = event.getX();
            lastPanScreenY = event.getY();
        } else if (primaryGestureActive) {
            controller.onPrimaryDragged(event.getX(), event.getY());
            redraw();
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        if (panGestureActive) {
            panGestureActive = false;
            setCursor(spacePanning ? Cursor.OPEN_HAND : Cursor.DEFAULT);
        } else if (primaryGestureActive) {
            primaryGestureActive = false;
            controller.onPrimaryReleased(event.getX(), event.getY());
            redraw();
        }
    }

    private void handleScroll(ScrollEvent event) {
        if (event.getDeltaY() == 0) {
            return;
        }
        double factor = event.getDeltaY() > 0 ? WHEEL_ZOOM_STEP : 1 / WHEEL_ZOOM_STEP;
        controller.zoomBy(factor, event.getX(), event.getY());
    }

    private void startPanGesture(MouseEvent event) {
        panGestureActive = true;
        lastPanScreenX = event.getX();
        lastPanScreenY = event.getY();
        setCursor(Cursor.CLOSED_HAND);
    }
}
