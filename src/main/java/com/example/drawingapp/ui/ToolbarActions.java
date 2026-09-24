package com.example.drawingapp.ui;

import com.example.drawingapp.model.enums.DrawingTool;

/**
 * The commands a toolbar can trigger.
 *
 * <p>Letting the toolbar depend on this interface instead of on the controllers keeps it a
 * passive view and avoids a circular bean dependency with {@link MainView}, which implements it.
 */
public interface ToolbarActions {

    void onNew();

    void onOpen();

    void onSave();

    void onToolSelected(DrawingTool tool);

    void onZoomIn();

    void onZoomOut();

    void onResetZoom();
}
