package com.example.drawingapp.service;

import com.example.drawingapp.config.ApplicationConfig;
import com.example.drawingapp.exception.DrawingFileException;
import com.example.drawingapp.model.DrawingDocument;
import com.example.drawingapp.model.DrawingObject;
import com.example.drawingapp.model.EllipseObject;
import com.example.drawingapp.model.LineObject;
import com.example.drawingapp.model.RectangleObject;
import com.example.drawingapp.model.TextObject;
import com.example.drawingapp.model.enums.ObjectType;
import com.example.drawingapp.repository.DrawingFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Persistence tests. They exercise the real {@link com.fasterxml.jackson.databind.ObjectMapper}
 * configuration from {@link ApplicationConfig} rather than a hand-built one, so the tests fail if
 * the production serialization settings change.
 */
class FileServiceTest {

    private static final double TOLERANCE = 0.0001;

    @TempDir
    Path tempDir;

    private FileService fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileService(new DrawingFileRepository(new ApplicationConfig().objectMapper()));
    }

    @Test
    @DisplayName("Every object type survives a save and open cycle")
    void savesAndReopensAllObjectTypes() throws DrawingFileException {
        DrawingDocument document = new DrawingDocument();
        document.add(RectangleObject.fromDrag(10, 20, 110, 70));
        document.add(EllipseObject.fromDrag(0, 0, 50, 50));
        document.add(LineObject.fromDrag(5, 5, 95, 45));
        document.add(TextObject.at(200, 100, "hello world"));

        Path saved = fileService.save(document, tempDir.resolve("shapes"));
        DrawingDocument reopened = fileService.open(saved);

        assertEquals(4, reopened.size());
        List<DrawingObject> objects = reopened.getObjects();

        RectangleObject rectangle = assertInstanceOf(RectangleObject.class, objects.get(0));
        assertEquals(10, rectangle.getX(), TOLERANCE);
        assertEquals(100, rectangle.getWidth(), TOLERANCE);
        assertEquals(50, rectangle.getHeight(), TOLERANCE);

        assertInstanceOf(EllipseObject.class, objects.get(1));

        LineObject line = assertInstanceOf(LineObject.class, objects.get(2));
        assertEquals(95, line.getEndX(), TOLERANCE);
        assertEquals(45, line.getEndY(), TOLERANCE);

        TextObject text = assertInstanceOf(TextObject.class, objects.get(3));
        assertEquals("hello world", text.getText());
        assertEquals(ObjectType.TEXT, text.getType());
    }

    @Test
    void preservesObjectIdentityAndOrder() throws DrawingFileException {
        DrawingDocument document = new DrawingDocument();
        RectangleObject first = RectangleObject.fromDrag(0, 0, 10, 10);
        RectangleObject second = RectangleObject.fromDrag(20, 20, 30, 30);
        document.add(first);
        document.add(second);

        DrawingDocument reopened = fileService.open(fileService.save(document, tempDir.resolve("order")));

        assertEquals(first.getId(), reopened.getObjects().get(0).getId());
        assertEquals(second.getId(), reopened.getObjects().get(1).getId());
    }

    @Test
    @DisplayName("The drawing extension is appended when the user omits it")
    void appendsTheDrawingExtension() throws DrawingFileException {
        Path saved = fileService.save(new DrawingDocument(), tempDir.resolve("sketch"));

        assertEquals("sketch.drawing.json", saved.getFileName().toString());
        assertTrue(Files.exists(saved));
    }

    @Test
    void doesNotDoubleUpTheExtension() {
        Path alreadyNamed = tempDir.resolve("sketch.drawing.json");

        assertEquals(alreadyNamed, FileService.ensureExtension(alreadyNamed));
        assertEquals("sketch.drawing.json",
                FileService.ensureExtension(tempDir.resolve("sketch.json")).getFileName().toString());
    }

    @Test
    void writesTheDocumentedJsonStructure() throws Exception {
        DrawingDocument document = new DrawingDocument();
        document.add(RectangleObject.fromDrag(100, 100, 300, 200));

        String json = Files.readString(fileService.save(document, tempDir.resolve("structure")))
                .replace(" ", "");

        assertTrue(json.contains("\"version\":1"));
        assertTrue(json.contains("\"objects\""));
        assertTrue(json.contains("\"type\":\"RECTANGLE\""));
        assertTrue(json.contains("\"width\":200.0"));
    }

    @Test
    @DisplayName("Malformed JSON is reported, not thrown as a crash")
    void rejectsMalformedJson() throws IOException {
        Path broken = writeFile("broken.drawing.json", "{ this is not json ");

        DrawingFileException exception = assertThrows(DrawingFileException.class,
                () -> fileService.open(broken));
        assertTrue(exception.getMessage().contains("broken.drawing.json"));
    }

    @Test
    void rejectsAnUnsupportedObjectType() throws IOException {
        Path file = writeFile("unsupported.drawing.json",
                "{\"version\":1,\"objects\":[{\"id\":\"7f000001-0000-4000-8000-000000000001\","
                        + "\"type\":\"HEXAGON\",\"x\":0,\"y\":0}]}");

        DrawingFileException exception = assertThrows(DrawingFileException.class,
                () -> fileService.open(file));
        assertTrue(exception.getMessage().contains("HEXAGON"));
    }

    @Test
    @DisplayName("Missing optional fields fall back to safe defaults instead of failing")
    void toleratesMissingFields() throws Exception {
        Path file = writeFile("partial.drawing.json",
                "{\"version\":1,\"objects\":[{\"type\":\"RECTANGLE\",\"x\":10,\"y\":20}]}");

        DrawingDocument document = fileService.open(file);

        RectangleObject rectangle = assertInstanceOf(RectangleObject.class, document.getObjects().get(0));
        assertEquals(10, rectangle.getX(), TOLERANCE);
        assertEquals(0, rectangle.getWidth(), TOLERANCE);
        assertEquals(2.0, rectangle.getStrokeWidth(), TOLERANCE);
        assertNotNull(rectangle.getId());
    }

    @Test
    void ignoresUnknownFields() throws Exception {
        Path file = writeFile("extra.drawing.json",
                "{\"version\":1,\"unexpected\":true,\"objects\":[{\"type\":\"LINE\",\"x\":0,\"y\":0,"
                        + "\"endX\":10,\"endY\":10,\"opacity\":0.5}]}");

        assertEquals(1, fileService.open(file).size());
    }

    @Test
    void rejectsAFileFromANewerVersion() throws IOException {
        Path file = writeFile("future.drawing.json", "{\"version\":99,\"objects\":[]}");

        assertThrows(DrawingFileException.class, () -> fileService.open(file));
    }

    @Test
    void rejectsAnEmptyFile() throws IOException {
        Path file = writeFile("empty.drawing.json", "");

        assertThrows(DrawingFileException.class, () -> fileService.open(file));
    }

    @Test
    void reportsAMissingFile() {
        assertThrows(DrawingFileException.class,
                () -> fileService.open(tempDir.resolve("does-not-exist.drawing.json")));
    }

    private Path writeFile(String name, String content) throws IOException {
        Path file = tempDir.resolve(name);
        Files.writeString(file, content);
        return file;
    }
}
