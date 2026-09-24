package com.example.drawingapp.exception;

/**
 * Thrown when a drawing file cannot be read or written.
 *
 * <p>Checked on purpose: opening and saving are the two operations where failure is expected
 * rather than exceptional, and the compiler should force the UI layer to present a message
 * instead of letting the application die. The message is written to be shown directly to the
 * user, so it never contains stack details.
 */
public class DrawingFileException extends Exception {

    public DrawingFileException(String message) {
        super(message);
    }

    public DrawingFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
