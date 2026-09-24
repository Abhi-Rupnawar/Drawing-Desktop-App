package com.example.drawingapp.model;

import com.example.drawingapp.model.enums.ObjectType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextObjectTest {

    private static final double TOLERANCE = 0.0001;

    @Test
    void reportsItsType() {
        assertEquals(ObjectType.TEXT, TextObject.at(0, 0, "hello").getType());
    }

    @Test
    @DisplayName("Bounds start at the anchor and grow with the text length")
    void estimatesBoundsFromTheText() {
        TextObject shortText = TextObject.at(30, 40, "hi");
        TextObject longText = TextObject.at(30, 40, "a much longer caption");

        assertEquals(30, shortText.getBounds().x(), TOLERANCE);
        assertEquals(40, shortText.getBounds().y(), TOLERANCE);
        assertTrue(shortText.getBounds().width() > 0);
        assertTrue(longText.getBounds().width() > shortText.getBounds().width());
        assertEquals(shortText.getBounds().height(), longText.getBounds().height(), TOLERANCE);
    }

    @Test
    void hitTestsAgainstItsBounds() {
        TextObject text = TextObject.at(0, 0, "selectable");
        Bounds bounds = text.getBounds();

        assertTrue(text.containsPoint(bounds.width() / 2, bounds.height() / 2, 0));
        assertFalse(text.containsPoint(bounds.maxX() + 20, bounds.height() / 2, 0));
    }

    @Test
    @DisplayName("An unusable font size from a file falls back to the default")
    void fallsBackToTheDefaultFontSize() {
        TextObject text = new TextObject(UUID.randomUUID(), 0, 0, "x", 0, null, 0);

        assertEquals(StyleDefaults.FONT_SIZE, text.getFontSize(), TOLERANCE);
        assertEquals(StyleDefaults.STROKE_COLOR, text.getStrokeColor());
    }

    @Test
    void treatsMissingTextAsEmpty() {
        TextObject text = new TextObject(null, 0, 0, null, 12, "#112233", 1);

        assertEquals("", text.getText());
        assertTrue(text.getBounds().width() > 0);
    }

    @Test
    void movesByDelta() {
        TextObject text = TextObject.at(5, 5, "move me");

        text.moveBy(10, 10);

        assertEquals(15, text.getX(), TOLERANCE);
        assertEquals(15, text.getY(), TOLERANCE);
    }
}
