package com.example.drawingapp.controller;

import com.example.drawingapp.model.DrawingObject;
import com.example.drawingapp.model.EllipseObject;
import com.example.drawingapp.model.LineObject;
import com.example.drawingapp.model.RectangleObject;
import com.example.drawingapp.model.TextObject;
import com.example.drawingapp.model.enums.DrawingTool;
import com.example.drawingapp.service.DrawingService;
import com.example.drawingapp.service.SelectionService;
import com.example.drawingapp.service.ViewTransformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Interaction tests. The controller takes screen coordinates and primitives only, so the whole
 * gesture pipeline - including the screen-to-world conversion - is testable without starting a
 * JavaFX toolkit.
 */
class DrawingControllerTest {

    private static final double TOLERANCE = 0.0001;

    private DrawingService drawingService;
    private SelectionService selectionService;
    private ViewTransformService viewTransform;
    private DrawingController controller;

    @BeforeEach
    void setUp() {
        drawingService = new DrawingService();
        selectionService = new SelectionService();
        viewTransform = new ViewTransformService();
        controller = new DrawingController(drawingService, selectionService, viewTransform);
    }

    @Test
    void createsARectangleFromADragGesture() {
        controller.setActiveTool(DrawingTool.RECTANGLE);

        drag(100, 100, 250, 200);

        RectangleObject rectangle = assertInstanceOf(RectangleObject.class, onlyObject());
        assertEquals(100, rectangle.getX(), TOLERANCE);
        assertEquals(150, rectangle.getWidth(), TOLERANCE);
        assertEquals(100, rectangle.getHeight(), TOLERANCE);
    }

    @Test
    void createsEllipsesAndLines() {
        controller.setActiveTool(DrawingTool.ELLIPSE);
        drag(0, 0, 80, 40);
        assertInstanceOf(EllipseObject.class, onlyObject());

        controller.setActiveTool(DrawingTool.LINE);
        drag(10, 10, 90, 90);
        assertEquals(2, drawingService.objectCount());
        assertInstanceOf(LineObject.class, drawingService.getObjects().get(1));
    }

    @Test
    @DisplayName("A shape is previewed while dragging and only committed on release")
    void previewsTheShapeBeingDrawn() {
        controller.setActiveTool(DrawingTool.RECTANGLE);

        controller.onPrimaryPressed(10, 10);
        controller.onPrimaryDragged(60, 40);

        assertTrue(controller.getPreviewObject().isPresent());
        assertEquals(0, drawingService.objectCount());

        controller.onPrimaryReleased(60, 40);

        assertTrue(controller.getPreviewObject().isEmpty());
        assertEquals(1, drawingService.objectCount());
    }

    @Test
    @DisplayName("A click without a drag does not create an accidental shape")
    void ignoresAnEmptyDrag() {
        controller.setActiveTool(DrawingTool.RECTANGLE);

        drag(50, 50, 50, 50);

        assertEquals(0, drawingService.objectCount());
        assertFalse(drawingService.isDirty());
    }

    @Test
    @DisplayName("Screen coordinates are converted to world coordinates before shapes are created")
    void createsShapesInWorldCoordinatesWhenZoomedAndPanned() {
        viewTransform.zoomBy(2.0, 0, 0);
        viewTransform.panBy(100, 50);
        controller.setActiveTool(DrawingTool.RECTANGLE);

        drag(300, 250, 500, 450);

        RectangleObject rectangle = assertInstanceOf(RectangleObject.class, onlyObject());
        assertEquals(100, rectangle.getX(), TOLERANCE);
        assertEquals(100, rectangle.getY(), TOLERANCE);
        assertEquals(100, rectangle.getWidth(), TOLERANCE);
        assertEquals(100, rectangle.getHeight(), TOLERANCE);
    }

    @Test
    void selectsAnObjectByClickingIt() {
        RectangleObject rectangle = RectangleObject.fromDrag(0, 0, 100, 100);
        drawingService.addObject(rectangle);
        controller.setActiveTool(DrawingTool.SELECT);

        controller.onPrimaryPressed(50, 50);

        assertSame(rectangle, selectionService.getSelected().orElseThrow());
    }

    @Test
    void deselectsWhenClickingEmptyCanvas() {
        RectangleObject rectangle = RectangleObject.fromDrag(0, 0, 100, 100);
        drawingService.addObject(rectangle);
        controller.setActiveTool(DrawingTool.SELECT);
        controller.onPrimaryPressed(50, 50);

        controller.onPrimaryPressed(400, 400);

        assertTrue(selectionService.getSelected().isEmpty());
    }

    @Test
    void dragsTheSelectedObject() {
        RectangleObject rectangle = RectangleObject.fromDrag(0, 0, 100, 100);
        drawingService.addObject(rectangle);
        controller.setActiveTool(DrawingTool.SELECT);

        controller.onPrimaryPressed(50, 50);
        controller.onPrimaryDragged(80, 90);
        controller.onPrimaryReleased(80, 90);

        assertEquals(30, rectangle.getX(), TOLERANCE);
        assertEquals(40, rectangle.getY(), TOLERANCE);
    }

    @Test
    @DisplayName("Dragging moves by the same world distance at any zoom level")
    void dragsCorrectlyWhileZoomed() {
        RectangleObject rectangle = RectangleObject.fromDrag(0, 0, 100, 100);
        drawingService.addObject(rectangle);
        controller.setActiveTool(DrawingTool.SELECT);
        viewTransform.zoomBy(2.0, 0, 0);

        controller.onPrimaryPressed(100, 100);
        controller.onPrimaryDragged(200, 160);
        controller.onPrimaryReleased(200, 160);

        assertEquals(50, rectangle.getX(), TOLERANCE);
        assertEquals(30, rectangle.getY(), TOLERANCE);
    }

    @Test
    void createsTextFromTheSuppliedInput() {
        controller.setTextInputProvider(() -> Optional.of("annotation"));
        controller.setActiveTool(DrawingTool.TEXT);

        controller.onPrimaryPressed(120, 240);

        TextObject text = assertInstanceOf(TextObject.class, onlyObject());
        assertEquals("annotation", text.getText());
        assertEquals(120, text.getX(), TOLERANCE);
        assertEquals(240, text.getY(), TOLERANCE);
        assertTrue(selectionService.isSelected(text));
    }

    @Test
    @DisplayName("Cancelling or rejecting the text input creates nothing")
    void createsNoTextWhenTheInputIsRejected() {
        controller.setTextInputProvider(Optional::empty);
        controller.setActiveTool(DrawingTool.TEXT);

        controller.onPrimaryPressed(10, 10);

        assertEquals(0, drawingService.objectCount());
    }

    @Test
    void switchingToADrawingToolClearsTheSelection() {
        RectangleObject rectangle = RectangleObject.fromDrag(0, 0, 100, 100);
        drawingService.addObject(rectangle);
        controller.setActiveTool(DrawingTool.SELECT);
        controller.onPrimaryPressed(50, 50);

        controller.setActiveTool(DrawingTool.ELLIPSE);

        assertTrue(selectionService.getSelected().isEmpty());
        assertEquals(DrawingTool.ELLIPSE, controller.getActiveTool());
    }

    @Test
    void deletesTheSelectedObject() {
        RectangleObject rectangle = RectangleObject.fromDrag(0, 0, 100, 100);
        drawingService.addObject(rectangle);
        controller.setActiveTool(DrawingTool.SELECT);
        controller.onPrimaryPressed(50, 50);

        controller.deleteSelected();

        assertEquals(0, drawingService.objectCount());
        assertTrue(selectionService.getSelected().isEmpty());
    }

    private void drag(double fromX, double fromY, double toX, double toY) {
        controller.onPrimaryPressed(fromX, fromY);
        controller.onPrimaryDragged(toX, toY);
        controller.onPrimaryReleased(toX, toY);
    }

    private DrawingObject onlyObject() {
        assertEquals(1, drawingService.objectCount());
        return drawingService.getObjects().get(0);
    }
}
