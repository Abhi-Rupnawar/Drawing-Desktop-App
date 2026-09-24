package com.example.drawingapp.ui;

import com.example.drawingapp.model.Bounds;
import com.example.drawingapp.model.DrawingObject;
import com.example.drawingapp.model.EllipseObject;
import com.example.drawingapp.model.LineObject;
import com.example.drawingapp.model.RectangleObject;
import com.example.drawingapp.model.TextObject;
import com.example.drawingapp.service.ViewTransformService;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Draws a document onto a JavaFX canvas.
 *
 * <p>This is the only place that knows how a {@link DrawingObject} looks. The model stays pure
 * data, and adding a new visual style never touches the domain classes.
 *
 * <p>Rendering applies the view transform to the graphics context once, then draws every object
 * in world coordinates. That is far simpler and less error-prone than transforming each object's
 * coordinates by hand, and it guarantees that what is drawn matches what hit testing computes.
 */
@Component
public class CanvasRenderer {

    private static final Color BACKGROUND_COLOR = Color.web("#FDFDFD");
    private static final Color GRID_COLOR = Color.web("#E8E8EF");
    private static final Color SELECTION_COLOR = Color.web("#4C6EF5");

    /** Grid spacing in world units. The grid only exists to make panning and zooming legible. */
    private static final double GRID_SPACING = 50.0;
    private static final double MIN_ZOOM_FOR_GRID = 0.4;

    /** Screen-space constants; divided by the zoom so they keep a constant on-screen size. */
    private static final double GRID_LINE_WIDTH = 1.0;
    private static final double SELECTION_LINE_WIDTH = 1.5;
    private static final double SELECTION_PADDING = 5.0;
    private static final double[] SELECTION_DASHES = {6.0, 4.0};

    private final ViewTransformService viewTransform;

    public CanvasRenderer(ViewTransformService viewTransform) {
        this.viewTransform = viewTransform;
    }

    /**
     * Repaints the whole canvas. A full redraw is intentional: for the object counts this
     * application targets it is fast, and it removes a whole class of stale-pixel bugs that a
     * dirty-region strategy would introduce.
     *
     * @param preview  the shape currently being dragged out, or {@code null}
     * @param selected the selected object, or {@code null}
     */
    public void render(GraphicsContext gc, double width, double height,
                       List<DrawingObject> objects, DrawingObject preview, DrawingObject selected) {

        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, width, height);

        gc.save();
        gc.translate(viewTransform.getPanX(), viewTransform.getPanY());
        gc.scale(viewTransform.getZoom(), viewTransform.getZoom());

        drawGrid(gc, width, height);
        objects.forEach(object -> drawObject(gc, object));

        if (preview != null) {
            drawObject(gc, preview);
        }
        if (selected != null) {
            drawSelectionIndicator(gc, selected);
        }

        gc.restore();
    }

    private void drawGrid(GraphicsContext gc, double width, double height) {
        if (viewTransform.getZoom() < MIN_ZOOM_FOR_GRID) {
            return;
        }

        double left = viewTransform.toWorldX(0);
        double top = viewTransform.toWorldY(0);
        double right = viewTransform.toWorldX(width);
        double bottom = viewTransform.toWorldY(height);

        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(scaled(GRID_LINE_WIDTH));

        for (double x = snapToGrid(left); x <= right; x += GRID_SPACING) {
            gc.strokeLine(x, top, x, bottom);
        }
        for (double y = snapToGrid(top); y <= bottom; y += GRID_SPACING) {
            gc.strokeLine(left, y, right, y);
        }
    }

    private void drawObject(GraphicsContext gc, DrawingObject object) {
        gc.setStroke(Color.web(object.getStrokeColor()));
        gc.setLineWidth(object.getStrokeWidth());

        if (object instanceof RectangleObject rectangle) {
            gc.strokeRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
        } else if (object instanceof EllipseObject ellipse) {
            gc.strokeOval(ellipse.getX(), ellipse.getY(), ellipse.getWidth(), ellipse.getHeight());
        } else if (object instanceof LineObject line) {
            gc.strokeLine(line.getX(), line.getY(), line.getEndX(), line.getEndY());
        } else if (object instanceof TextObject text) {
            drawText(gc, text);
        }
    }

    private void drawText(GraphicsContext gc, TextObject text) {
        gc.setFill(Color.web(text.getStrokeColor()));
        gc.setFont(Font.font(text.getFontSize()));
        gc.setTextBaseline(VPos.TOP);
        gc.fillText(text.getText(), text.getX(), text.getY());
    }

    private void drawSelectionIndicator(GraphicsContext gc, DrawingObject selected) {
        Bounds bounds = selected.getBounds().expanded(scaled(SELECTION_PADDING));

        gc.save();
        gc.setStroke(SELECTION_COLOR);
        gc.setLineWidth(scaled(SELECTION_LINE_WIDTH));
        gc.setLineDashes(scaled(SELECTION_DASHES[0]), scaled(SELECTION_DASHES[1]));
        gc.strokeRect(bounds.x(), bounds.y(), bounds.width(), bounds.height());
        gc.restore();
    }

    /** Converts a screen-pixel size into world units so it renders at a constant on-screen size. */
    private double scaled(double screenValue) {
        return screenValue / viewTransform.getZoom();
    }

    private static double snapToGrid(double worldValue) {
        return Math.floor(worldValue / GRID_SPACING) * GRID_SPACING;
    }
}
