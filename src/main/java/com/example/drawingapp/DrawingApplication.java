package com.example.drawingapp;

import com.example.drawingapp.ui.JavaFxApplication;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the desktop application.
 *
 * <p>The class is the Spring Boot configuration root, but {@code main} hands control straight to
 * JavaFX: {@link JavaFxApplication} then starts the Spring context inside the JavaFX lifecycle.
 * Launching this way, rather than from {@code SpringApplication.run}, keeps the toolkit
 * initialisation and the shutdown hooks in the order JavaFX expects.
 */
@SpringBootApplication
public class DrawingApplication {

    public static void main(String[] args) {
        Application.launch(JavaFxApplication.class, args);
    }
}
