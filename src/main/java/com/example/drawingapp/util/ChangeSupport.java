package com.example.drawingapp.util;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Minimal listener registry used by the stateful services to tell the UI that a repaint is due.
 *
 * <p>This keeps the services free of any JavaFX property API while still letting the canvas and
 * the status bar react to changes they did not cause themselves (for example a file being
 * opened). Listeners are invoked on the calling thread, which is always the JavaFX application
 * thread in this application.
 */
public final class ChangeSupport {

    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    public void addListener(Runnable listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public void fire() {
        listeners.forEach(Runnable::run);
    }
}
