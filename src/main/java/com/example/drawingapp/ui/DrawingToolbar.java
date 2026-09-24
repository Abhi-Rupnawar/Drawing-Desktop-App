package com.example.drawingapp.ui;

import com.example.drawingapp.model.enums.DrawingTool;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;

import java.util.EnumMap;
import java.util.Map;

/**
 * The application toolbar: file commands, tool selection and zoom commands.
 *
 * <p>Purely a view. It reports every interaction through {@link ToolbarActions} and exposes
 * {@link #selectTool} so that keyboard shortcuts can keep the buttons in sync with the active
 * tool.
 */
public class DrawingToolbar extends ToolBar {

    private static final Insets PADDING = new Insets(6, 10, 6, 10);

    private final Map<DrawingTool, ToggleButton> toolButtons = new EnumMap<>(DrawingTool.class);
    private final ToggleGroup toolGroup = new ToggleGroup();

    public DrawingToolbar(ToolbarActions actions) {
        setPadding(PADDING);

        getItems().addAll(
                fileButton("New", "Start a new drawing (Ctrl+N)", actions::onNew),
                fileButton("Open", "Open a drawing (Ctrl+O)", actions::onOpen),
                fileButton("Save", "Save the drawing (Ctrl+S)", actions::onSave),
                new Separator());

        for (DrawingTool tool : DrawingTool.values()) {
            getItems().add(createToolButton(tool));
        }

        getItems().addAll(
                new Separator(),
                fileButton("Zoom In", "Zoom in (+)", actions::onZoomIn),
                fileButton("Zoom Out", "Zoom out (-)", actions::onZoomOut),
                fileButton("Reset Zoom", "Reset zoom to 100% (0)", actions::onResetZoom));

        toolGroup.selectedToggleProperty().addListener((observable, previous, selected) -> {
            if (selected == null) {
                // A toggle group allows an empty selection; a drawing tool is always active.
                toolGroup.selectToggle(previous);
            } else {
                actions.onToolSelected((DrawingTool) selected.getUserData());
            }
        });

        selectTool(DrawingTool.SELECT);
    }

    /** Activates a tool from outside the toolbar, for example from a keyboard shortcut. */
    public void selectTool(DrawingTool tool) {
        toolGroup.selectToggle(toolButtons.get(tool));
    }

    private ToggleButton createToolButton(DrawingTool tool) {
        ToggleButton button = new ToggleButton(tool.displayName());
        button.setUserData(tool);
        button.setToggleGroup(toolGroup);
        button.setTooltip(new Tooltip(tool.displayName() + " tool"));
        toolButtons.put(tool, button);
        return button;
    }

    private static Button fileButton(String label, String tooltip, Runnable action) {
        Button button = new Button(label);
        button.setTooltip(new Tooltip(tooltip));
        button.setOnAction(event -> action.run());
        return button;
    }
}
