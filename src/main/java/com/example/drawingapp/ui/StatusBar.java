package com.example.drawingapp.ui;

import com.example.drawingapp.service.DrawingService;
import com.example.drawingapp.service.ViewTransformService;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Bottom status bar showing the zoom level, the object count, the unsaved marker and the last
 * status message. It subscribes to the services rather than being pushed to, so it stays correct
 * no matter which part of the application caused the change.
 */
@Component
public class StatusBar extends HBox {

    private static final String READY_MESSAGE = "Ready";
    private static final double SPACING = 10;
    private static final Insets PADDING = new Insets(4, 10, 4, 10);
    private static final int PERCENT = 100;

    private final Label zoomLabel = new Label();
    private final Label objectCountLabel = new Label();
    private final Label messageLabel = new Label(READY_MESSAGE);

    private final DrawingService drawingService;
    private final ViewTransformService viewTransform;

    public StatusBar(DrawingService drawingService, ViewTransformService viewTransform) {
        this.drawingService = drawingService;
        this.viewTransform = viewTransform;

        setSpacing(SPACING);
        setPadding(PADDING);
        setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(
                zoomLabel, new Separator(Orientation.VERTICAL),
                objectCountLabel, new Separator(Orientation.VERTICAL),
                messageLabel);

        drawingService.addChangeListener(this::refresh);
        viewTransform.addChangeListener(this::refresh);
        refresh();
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    public void clearMessage() {
        messageLabel.setText(READY_MESSAGE);
    }

    private void refresh() {
        zoomLabel.setText(String.format(Locale.ROOT, "Zoom: %.0f%%", viewTransform.getZoom() * PERCENT));
        objectCountLabel.setText("Objects: " + drawingService.objectCount()
                + (drawingService.isDirty() ? "  (unsaved changes)" : ""));
    }
}
