package com.example.drawingapp.ui;

import com.example.drawingapp.service.FileService;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Every modal interaction with the user lives here.
 *
 * <p>Concentrating the dialogs in one component keeps {@link MainView} readable and keeps the
 * controllers free of JavaFX: they signal failure by throwing, and the view decides how to show
 * it. All methods must be called on the JavaFX application thread.
 */
@Component
public class DialogService {

    private static final int MAX_TEXT_LENGTH = 500;

    private static final ButtonType SAVE_BUTTON = new ButtonType("Save", ButtonBar.ButtonData.YES);
    private static final ButtonType DONT_SAVE_BUTTON = new ButtonType("Don't Save", ButtonBar.ButtonData.NO);
    private static final ButtonType CANCEL_BUTTON =
            new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

    private Window owner;

    /** Set once the primary stage exists, so dialogs are modal to the application window. */
    public void setOwner(Window owner) {
        this.owner = owner;
    }

    public Optional<Path> chooseOpenFile() {
        File file = drawingFileChooser("Open Drawing").showOpenDialog(owner);
        return Optional.ofNullable(file).map(File::toPath);
    }

    public Optional<Path> chooseSaveFile(String suggestedFileName) {
        FileChooser chooser = drawingFileChooser("Save Drawing");
        chooser.setInitialFileName(suggestedFileName);
        return Optional.ofNullable(chooser.showSaveDialog(owner)).map(File::toPath);
    }

    /**
     * Asks for the content of a new text object.
     *
     * @return the trimmed text, or empty if the user cancelled or the input was rejected
     */
    public Optional<String> promptForText() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.initOwner(owner);
        dialog.setTitle("Add Text");
        dialog.setHeaderText("Enter the text to place on the canvas");
        dialog.setContentText("Text:");

        Optional<String> input = dialog.showAndWait();
        if (input.isEmpty()) {
            return Optional.empty();
        }

        String text = input.get().trim();
        if (text.isEmpty()) {
            showError("Invalid text", "The text cannot be empty.");
            return Optional.empty();
        }
        if (text.length() > MAX_TEXT_LENGTH) {
            showError("Invalid text", "The text cannot be longer than " + MAX_TEXT_LENGTH + " characters.");
            return Optional.empty();
        }
        return Optional.of(text);
    }

    public void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(owner);
        alert.setTitle("Drawing Application");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public UnsavedChangesChoice confirmUnsavedChanges() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(owner);
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("Current drawing has unsaved changes.");
        alert.setContentText("Do you want to save before continuing?");
        alert.getButtonTypes().setAll(SAVE_BUTTON, DONT_SAVE_BUTTON, CANCEL_BUTTON);

        ButtonType choice = alert.showAndWait().orElse(CANCEL_BUTTON);
        if (choice == SAVE_BUTTON) {
            return UnsavedChangesChoice.SAVE;
        }
        if (choice == DONT_SAVE_BUTTON) {
            return UnsavedChangesChoice.DONT_SAVE;
        }
        return UnsavedChangesChoice.CANCEL;
    }

    private static FileChooser drawingFileChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Drawing files", "*" + FileService.FILE_EXTENSION),
                new FileChooser.ExtensionFilter("JSON files", "*.json"));
        return chooser;
    }
}
