package com.example.drawingapp.repository;

import com.example.drawingapp.exception.DrawingFileException;
import com.example.drawingapp.model.DrawingDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads and writes {@link DrawingDocument} files as JSON.
 *
 * <p>This is the only class that touches the file system. Keeping it separate from
 * {@code FileService} means the service can be tested against the real format while the
 * validation rules stay independent of the I/O mechanics.
 */
@Repository
public class DrawingFileRepository {

    private static final Logger log = LoggerFactory.getLogger(DrawingFileRepository.class);

    /** Guard against accidentally loading a huge or non-drawing file into memory. */
    private static final long MAX_FILE_SIZE_BYTES = 20L * 1024 * 1024;

    private final ObjectMapper objectMapper;

    public DrawingFileRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(DrawingDocument document, Path path) throws DrawingFileException {
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), document);
            log.info("Saved drawing with {} object(s) to {}", document.size(), path);
        } catch (IOException e) {
            log.error("Failed to save drawing to {}", path, e);
            throw new DrawingFileException("The drawing could not be saved to " + path.getFileName()
                    + ". Please check that the location is writable.", e);
        }
    }

    public DrawingDocument read(Path path) throws DrawingFileException {
        if (!Files.isRegularFile(path)) {
            throw new DrawingFileException("The file " + path.getFileName() + " does not exist.");
        }

        try {
            if (Files.size(path) > MAX_FILE_SIZE_BYTES) {
                throw new DrawingFileException("The file " + path.getFileName()
                        + " is too large to be a drawing file.");
            }
            DrawingDocument document = objectMapper.readValue(path.toFile(), DrawingDocument.class);
            if (document == null) {
                throw new DrawingFileException("The file " + path.getFileName() + " is empty.");
            }
            log.info("Opened drawing with {} object(s) from {}", document.size(), path);
            return document;
        } catch (InvalidTypeIdException e) {
            log.warn("Unsupported object type in {}: {}", path, e.getTypeId());
            throw new DrawingFileException("The file contains an unsupported object type: "
                    + e.getTypeId() + ".", e);
        } catch (JsonProcessingException e) {
            log.warn("Malformed drawing file {}", path, e);
            throw new DrawingFileException("The file " + path.getFileName()
                    + " is not a valid drawing file.", e);
        } catch (IOException e) {
            log.error("Failed to read drawing from {}", path, e);
            throw new DrawingFileException("The file " + path.getFileName()
                    + " could not be read.", e);
        }
    }
}
