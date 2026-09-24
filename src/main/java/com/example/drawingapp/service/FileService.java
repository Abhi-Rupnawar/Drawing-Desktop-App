package com.example.drawingapp.service;

import com.example.drawingapp.exception.DrawingFileException;
import com.example.drawingapp.model.DrawingDocument;
import com.example.drawingapp.repository.DrawingFileRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

/**
 * Application-level file rules: the drawing extension and what counts as a loadable document.
 *
 * <p>The repository already rejects malformed JSON and unknown object types. This service adds
 * the checks that are about the format rather than the syntax, such as refusing a file written
 * by a newer version of the application.
 */
@Service
public class FileService {

    public static final String FILE_EXTENSION = ".drawing.json";
    public static final String DEFAULT_FILE_NAME = "untitled" + FILE_EXTENSION;

    private final DrawingFileRepository repository;

    public FileService(DrawingFileRepository repository) {
        this.repository = repository;
    }

    /**
     * Saves the document, appending the drawing extension when the chosen name does not have it.
     *
     * @return the path actually written, so the caller can remember it for later saves
     */
    public Path save(DrawingDocument document, Path path) throws DrawingFileException {
        Path target = ensureExtension(path);
        repository.write(document, target);
        return target;
    }

    public DrawingDocument open(Path path) throws DrawingFileException {
        DrawingDocument document = repository.read(path);
        validate(document);
        return document;
    }

    public static Path ensureExtension(Path path) {
        String name = path.getFileName().toString();
        if (name.toLowerCase().endsWith(FILE_EXTENSION)) {
            return path;
        }
        return path.resolveSibling(stripJsonSuffix(name) + FILE_EXTENSION);
    }

    private static String stripJsonSuffix(String name) {
        return name.toLowerCase().endsWith(".json") ? name.substring(0, name.length() - ".json".length()) : name;
    }

    private static void validate(DrawingDocument document) throws DrawingFileException {
        if (document.getVersion() > DrawingDocument.CURRENT_VERSION) {
            throw new DrawingFileException("This drawing was created with a newer version of the "
                    + "application (file version " + document.getVersion() + ").");
        }
    }
}
