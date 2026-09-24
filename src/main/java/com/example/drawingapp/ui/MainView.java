package com.example.drawingapp.ui;

import com.example.drawingapp.controller.DocumentController;
import com.example.drawingapp.controller.DrawingController;
import com.example.drawingapp.exception.DrawingFileException;
import com.example.drawingapp.model.enums.DrawingTool;
import com.example.drawingapp.service.FileService;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Optional;

/**
 * The application window content: toolbar on top, canvas in the middle, status bar at the bottom.
 *
 * <p>It also owns the user-facing part of the file commands, because deciding whether to prompt,
 * what to show and which message to display is a view concern. The actual work is delegated to
 * {@link DocumentController}.
 */
@Component
public class MainView extends BorderPane implements ToolbarActions {

    private static final Logger log = LoggerFactory.getLogger(MainView.class);

    private final DrawingToolbar toolbar;
    private final DrawingCanvas canvas;
    private final StatusBar statusBar;
    private final DocumentController documentController;
    private final DrawingController drawingController;
    private final DialogService dialogService;

    public MainView(DrawingCanvas canvas,
                    StatusBar statusBar,
                    DocumentController documentController,
                    DrawingController drawingController,
                    DialogService dialogService) {
        this.canvas = canvas;
        this.statusBar = statusBar;
        this.documentController = documentController;
        this.drawingController = drawingController;
        this.dialogService = dialogService;
        this.toolbar = new DrawingToolbar(this);

        drawingController.setTextInputProvider(dialogService::promptForText);

        setTop(toolbar);
        setCenter(canvas);
        setBottom(statusBar);
    }

    /** Registers the keyboard shortcuts on the scene once it has been created. */
    public void installShortcuts(Scene scene) {
        scene.setOnKeyPressed(this::handleKeyPressed);
        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                canvas.setSpacePanning(false);
            }
        });
    }

    @Override
    public void onNew() {
        if (!confirmDiscardChanges()) {
            return;
        }
        documentController.newDrawing();
        toolbar.selectTool(DrawingTool.SELECT);
        statusBar.setMessage("New drawing");
    }

    @Override
    public void onOpen() {
        if (!confirmDiscardChanges()) {
            return;
        }
        dialogService.chooseOpenFile().ifPresent(this::openDrawing);
    }

    @Override
    public void onSave() {
        saveDrawing();
    }

    @Override
    public void onToolSelected(DrawingTool tool) {
        drawingController.setActiveTool(tool);
        statusBar.setMessage(tool.displayName() + " tool");
        canvas.redraw();
    }

    @Override
    public void onZoomIn() {
        drawingController.zoomIn(canvas.centerX(), canvas.centerY());
    }

    @Override
    public void onZoomOut() {
        drawingController.zoomOut(canvas.centerX(), canvas.centerY());
    }

    @Override
    public void onResetZoom() {
        drawingController.resetZoom();
    }

    /**
     * Offers to save pending work before a destructive command.
     *
     * @return true when the caller may continue, false when the user cancelled
     */
    public boolean confirmDiscardChanges() {
        if (!documentController.hasUnsavedChanges()) {
            return true;
        }
        return switch (dialogService.confirmUnsavedChanges()) {
            case SAVE -> saveDrawing();
            case DONT_SAVE -> true;
            case CANCEL -> false;
        };
    }

    private void openDrawing(Path path) {
        try {
            documentController.open(path);
            statusBar.setMessage("Opened " + path.getFileName());
        } catch (DrawingFileException e) {
            log.warn("Open failed for {}", path, e);
            dialogService.showError("Unable to open drawing", e.getMessage());
        }
    }

    /**
     * Saves to the current file, asking for a location the first time.
     *
     * @return true when the drawing was written to disk
     */
    private boolean saveDrawing() {
        Optional<Path> target = documentController.getCurrentPath().or(this::askForSaveLocation);
        if (target.isEmpty()) {
            return false;
        }

        Path path = target.get();
        try {
            documentController.save(path);
            statusBar.setMessage("Saved " + documentController.getCurrentPath()
                    .map(Path::getFileName).map(Path::toString).orElse(""));
            return true;
        } catch (DrawingFileException e) {
            log.warn("Save failed for {}", path, e);
            dialogService.showError("Unable to save drawing", e.getMessage());
            return false;
        }
    }

    private Optional<Path> askForSaveLocation() {
        return dialogService.chooseSaveFile(FileService.DEFAULT_FILE_NAME);
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.SPACE) {
            canvas.setSpacePanning(true);
            return;
        }
        if (event.isShortcutDown()) {
            handleShortcutKey(event.getCode());
            return;
        }
        handlePlainKey(event.getCode());
    }

    private void handleShortcutKey(KeyCode code) {
        switch (code) {
            case N -> onNew();
            case O -> onOpen();
            case S -> onSave();
            default -> { /* not a shortcut of this application */ }
        }
    }

    private void handlePlainKey(KeyCode code) {
        switch (code) {
            case DELETE, BACK_SPACE -> deleteSelection();
            case DIGIT0, NUMPAD0 -> onResetZoom();
            case PLUS, EQUALS, ADD -> onZoomIn();
            case MINUS, SUBTRACT -> onZoomOut();
            case V -> toolbar.selectTool(DrawingTool.SELECT);
            case R -> toolbar.selectTool(DrawingTool.RECTANGLE);
            case E -> toolbar.selectTool(DrawingTool.ELLIPSE);
            case L -> toolbar.selectTool(DrawingTool.LINE);
            case T -> toolbar.selectTool(DrawingTool.TEXT);
            default -> { /* not a shortcut of this application */ }
        }
    }

    private void deleteSelection() {
        drawingController.deleteSelected();
        statusBar.setMessage("Deleted selected object");
    }
}
