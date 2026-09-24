package com.example.drawingapp.controller;

import com.example.drawingapp.model.DrawingObject;
import com.example.drawingapp.model.EllipseObject;
import com.example.drawingapp.model.LineObject;
import com.example.drawingapp.model.Point;
import com.example.drawingapp.model.RectangleObject;
import com.example.drawingapp.model.TextObject;
import com.example.drawingapp.model.enums.DrawingTool;
import com.example.drawingapp.service.DrawingService;
import com.example.drawingapp.service.SelectionService;
import com.example.drawingapp.service.ViewTransformService;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Translates canvas gestures into model changes.
 *
 * <p>The controller is a small state machine over {@link InteractionMode}. It receives
 * <em>screen</em> coordinates and immediately converts them to world coordinates, so no
 * downstream code has to know about the current zoom or pan. It deliberately contains no JavaFX
 * types: everything it needs from the UI arrives as primitives or through
 * {@link TextInputProvider}, which keeps the interaction rules unit testable.
 */
@Component
public class DrawingController {

    /** Click slack in screen pixels, converted to world units before hit testing. */
    private static final double HIT_TOLERANCE_PX = 6.0;

    /** Drags smaller than this (in world units) are treated as accidental clicks. */
    private static final double MIN_SHAPE_SIZE = 2.0;

    private enum InteractionMode { IDLE, CREATING, MOVING }

    private final DrawingService drawingService;
    private final SelectionService selectionService;
    private final ViewTransformService viewTransform;

    private DrawingTool activeTool = DrawingTool.SELECT;
    private InteractionMode mode = InteractionMode.IDLE;
    private TextInputProvider textInputProvider = Optional::empty;

    private Point dragStartWorld;
    private Point lastDragWorld;
    private DrawingObject previewObject;

    public DrawingController(DrawingService drawingService,
                             SelectionService selectionService,
                             ViewTransformService viewTransform) {
        this.drawingService = drawingService;
        this.selectionService = selectionService;
        this.viewTransform = viewTransform;
    }

    public void setTextInputProvider(TextInputProvider textInputProvider) {
        this.textInputProvider = textInputProvider;
    }

    public DrawingTool getActiveTool() {
        return activeTool;
    }

    /** Switching to a creation tool cancels any in-progress gesture and the current selection. */
    public void setActiveTool(DrawingTool tool) {
        this.activeTool = tool;
        cancelGesture();
        if (tool != DrawingTool.SELECT) {
            selectionService.clear();
        }
    }

    /** The shape being dragged out right now, rendered as a live preview. */
    public Optional<DrawingObject> getPreviewObject() {
        return Optional.ofNullable(previewObject);
    }

    public void onPrimaryPressed(double screenX, double screenY) {
        Point world = viewTransform.toWorld(screenX, screenY);

        switch (activeTool) {
            case SELECT -> beginSelectionOrMove(world);
            case TEXT -> createTextAt(world);
            case RECTANGLE, ELLIPSE, LINE -> beginShapeCreation(world);
        }
    }

    public void onPrimaryDragged(double screenX, double screenY) {
        Point world = viewTransform.toWorld(screenX, screenY);

        switch (mode) {
            case CREATING -> previewObject = buildShape(dragStartWorld, world);
            case MOVING -> moveSelection(world);
            case IDLE -> { /* nothing is being manipulated */ }
        }
    }

    public void onPrimaryReleased(double screenX, double screenY) {
        if (mode == InteractionMode.CREATING) {
            commitShape(viewTransform.toWorld(screenX, screenY));
        }
        mode = InteractionMode.IDLE;
        previewObject = null;
        dragStartWorld = null;
        lastDragWorld = null;
    }

    public void deleteSelected() {
        selectionService.getSelected().ifPresent(selected -> {
            drawingService.removeObject(selected);
            selectionService.clear();
        });
    }

    public void zoomIn(double anchorScreenX, double anchorScreenY) {
        viewTransform.zoomIn(anchorScreenX, anchorScreenY);
    }

    public void zoomOut(double anchorScreenX, double anchorScreenY) {
        viewTransform.zoomOut(anchorScreenX, anchorScreenY);
    }

    public void zoomBy(double factor, double anchorScreenX, double anchorScreenY) {
        viewTransform.zoomBy(factor, anchorScreenX, anchorScreenY);
    }

    public void resetZoom() {
        viewTransform.reset();
    }

    public void panBy(double screenDx, double screenDy) {
        viewTransform.panBy(screenDx, screenDy);
    }

    private void beginSelectionOrMove(Point world) {
        double tolerance = viewTransform.toWorldDistance(HIT_TOLERANCE_PX);
        Optional<DrawingObject> hit =
                selectionService.selectAt(drawingService.getObjects(), world.x(), world.y(), tolerance);

        if (hit.isPresent()) {
            mode = InteractionMode.MOVING;
            lastDragWorld = world;
        } else {
            mode = InteractionMode.IDLE;
        }
    }

    private void beginShapeCreation(Point world) {
        mode = InteractionMode.CREATING;
        dragStartWorld = world;
        previewObject = null;
    }

    private void createTextAt(Point world) {
        mode = InteractionMode.IDLE;
        textInputProvider.requestText()
                .map(text -> TextObject.at(world.x(), world.y(), text))
                .ifPresent(textObject -> {
                    drawingService.addObject(textObject);
                    selectionService.select(textObject);
                });
    }

    private void moveSelection(Point world) {
        selectionService.getSelected().ifPresent(selected -> drawingService.moveObject(selected,
                world.x() - lastDragWorld.x(), world.y() - lastDragWorld.y()));
        lastDragWorld = world;
    }

    private void commitShape(Point endWorld) {
        DrawingObject shape = buildShape(dragStartWorld, endWorld);
        if (shape == null || isTooSmall(shape)) {
            return;
        }
        drawingService.addObject(shape);
        selectionService.select(shape);
    }

    private DrawingObject buildShape(Point start, Point end) {
        if (start == null) {
            return null;
        }
        return switch (activeTool) {
            case RECTANGLE -> RectangleObject.fromDrag(start.x(), start.y(), end.x(), end.y());
            case ELLIPSE -> EllipseObject.fromDrag(start.x(), start.y(), end.x(), end.y());
            case LINE -> LineObject.fromDrag(start.x(), start.y(), end.x(), end.y());
            case SELECT, TEXT -> null;
        };
    }

    private static boolean isTooSmall(DrawingObject shape) {
        if (shape instanceof LineObject line) {
            return Math.hypot(line.getEndX() - line.getX(), line.getEndY() - line.getY()) < MIN_SHAPE_SIZE;
        }
        return shape.getBounds().width() < MIN_SHAPE_SIZE && shape.getBounds().height() < MIN_SHAPE_SIZE;
    }

    private void cancelGesture() {
        mode = InteractionMode.IDLE;
        previewObject = null;
        dragStartWorld = null;
        lastDragWorld = null;
    }
}
