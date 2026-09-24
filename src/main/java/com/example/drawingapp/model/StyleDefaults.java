package com.example.drawingapp.model;

import java.util.regex.Pattern;

/**
 * Default style values and the sanitising helpers used by {@link DrawingObject}.
 *
 * <p>Style values may come from a file on disk, so they are validated here rather than trusted.
 * An unparsable colour would otherwise only fail later, inside the rendering layer.
 */
public final class StyleDefaults {

    public static final String STROKE_COLOR = "#1E1E1E";
    public static final double STROKE_WIDTH = 2.0;
    public static final double FONT_SIZE = 18.0;

    private static final double MIN_STROKE_WIDTH = 0.5;
    private static final double MAX_STROKE_WIDTH = 20.0;
    private static final double MIN_FONT_SIZE = 6.0;
    private static final double MAX_FONT_SIZE = 200.0;
    private static final Pattern HEX_COLOR = Pattern.compile("^#([0-9a-fA-F]{6}|[0-9a-fA-F]{8})$");

    private StyleDefaults() {
    }

    static String strokeColorOrDefault(String color) {
        return color != null && HEX_COLOR.matcher(color).matches() ? color : STROKE_COLOR;
    }

    static double strokeWidthOrDefault(double width) {
        return clampOrDefault(width, MIN_STROKE_WIDTH, MAX_STROKE_WIDTH, STROKE_WIDTH);
    }

    static double fontSizeOrDefault(double fontSize) {
        return clampOrDefault(fontSize, MIN_FONT_SIZE, MAX_FONT_SIZE, FONT_SIZE);
    }

    private static double clampOrDefault(double value, double min, double max, double fallback) {
        if (!Double.isFinite(value) || value <= 0) {
            return fallback;
        }
        return Math.min(max, Math.max(min, value));
    }
}
