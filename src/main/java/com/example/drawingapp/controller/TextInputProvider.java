package com.example.drawingapp.controller;

import java.util.Optional;

/**
 * Supplies the text for a new text object.
 *
 * <p>The controller needs to ask the user for a string, but must not depend on JavaFX dialogs.
 * The UI layer installs an implementation; tests install a stub. An empty result means the user
 * cancelled or entered something invalid, and no object is created.
 */
@FunctionalInterface
public interface TextInputProvider {

    Optional<String> requestText();
}
