package com.example.drawingapp.ui;

import com.example.drawingapp.DrawingApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Bridges the JavaFX and Spring lifecycles.
 *
 * <p>The Spring context is started in {@link #init()}, which JavaFX runs before {@link #start},
 * so every bean exists by the time the scene is built. The context is closed in {@link #stop()}.
 * Spring is used purely as a dependency injection container here: no web server is started and
 * the application never talks to localhost.
 */
public class JavaFxApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(JavaFxApplication.class);

    private static final String WINDOW_TITLE = "2D Drawing Application";
    private static final double INITIAL_WIDTH = 1200;
    private static final double INITIAL_HEIGHT = 800;
    private static final double MINIMUM_WIDTH = 640;
    private static final double MINIMUM_HEIGHT = 480;

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        context = new SpringApplicationBuilder(DrawingApplication.class)
                .web(WebApplicationType.NONE)
                .run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage stage) {
        MainView mainView = context.getBean(MainView.class);
        context.getBean(DialogService.class).setOwner(stage);

        Scene scene = new Scene(mainView, INITIAL_WIDTH, INITIAL_HEIGHT);
        mainView.installShortcuts(scene);

        stage.setTitle(WINDOW_TITLE);
        stage.setScene(scene);
        stage.setMinWidth(MINIMUM_WIDTH);
        stage.setMinHeight(MINIMUM_HEIGHT);
        stage.setOnCloseRequest(event -> {
            if (!mainView.confirmDiscardChanges()) {
                event.consume();
            }
        });
        stage.show();

        log.info("Drawing application window ready");
    }

    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
        Platform.exit();
    }
}
