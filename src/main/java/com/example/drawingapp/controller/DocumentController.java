package com.example.drawingapp.controller;

import com.example.drawingapp.exception.DrawingFileException;
import com.example.drawingapp.model.DrawingDocument;
import com.example.drawingapp.service.DrawingService;
import com.example.drawingapp.service.FileService;
import com.example.drawingapp.service.SelectionService;
import com.example.drawingapp.service.ViewTransformService;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Document-level commands: new, open and save.
 *
 * <p>Kept separate from {@link DrawingController} so that neither class grows into a catch-all
 * controller: one owns canvas gestures, the other owns the document lifecycle. Dialogs and
 * alerts stay in the UI layer; this class only reports success by returning, or failure by
 * throwing {@link DrawingFileException}.
 */
@Component
public class DocumentController {

    private final DrawingService drawingService;
    private final SelectionService selectionService;
    private final ViewTransformService viewTransform;
    private final FileService fileService;

    private Path currentPath;

    public DocumentController(DrawingService drawingService,
                              SelectionService selectionService,
                              ViewTransformService viewTransform,
                              FileService fileService) {
        this.drawingService = drawingService;
        this.selectionService = selectionService;
        this.viewTransform = viewTransform;
        this.fileService = fileService;
    }

    public void newDrawing() {
        drawingService.newDocument();
        selectionService.clear();
        viewTransform.reset();
        currentPath = null;
    }

    public void open(Path path) throws DrawingFileException {
        DrawingDocument document = fileService.open(path);
        drawingService.replaceDocument(document);
        selectionService.clear();
        viewTransform.reset();
        currentPath = path;
    }

    public void save(Path path) throws DrawingFileException {
        currentPath = fileService.save(drawingService.getDocument(), path);
        drawingService.markSaved();
    }

    /** The file the drawing was last opened from or saved to, if any. */
    public Optional<Path> getCurrentPath() {
        return Optional.ofNullable(currentPath);
    }

    public boolean hasUnsavedChanges() {
        return drawingService.isDirty();
    }
}
