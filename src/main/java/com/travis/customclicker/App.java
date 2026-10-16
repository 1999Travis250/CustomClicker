package com.travis.customclicker;

import com.travis.customclicker.controller.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.Objects;

public class App extends Application {
    // Loads the existing JavaFX view and connects window and tray lifecycle actions.
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/views/main-view.fxml"));
        Parent content = loader.load();
        Group root = new Group(content);
        Scene scene = new Scene(root, 1200, 800);

        scene.setFill(Color.TRANSPARENT);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.setResizable(false);
        setWindowIcons(stage);

        MainController controller = loader.getController();
        Platform.setImplicitExit(false);

        stage.setOnCloseRequest(event -> {
            event.consume();
            controller.handleWindowCloseRequest();
        });
        stage.show();

        controller.applyInitialUiScale();
        controller.applyInitialAlwaysOnTop();
        controller.initializeTray();
        stage.centerOnScreen();
    }

    // Supplies Windows with several resolutions of the existing application artwork.
    private void setWindowIcons(Stage stage) {
        for (int size : new int[]{16, 32, 48, 256}) {
            String path = "/icons/customclicker-" + size + ".png";
            URL resource = Objects.requireNonNull(App.class.getResource(path), "Missing icon: " + path);
            stage.getIcons().add(new Image(resource.toExternalForm()));
        }
    }

    // Launches the JavaFX application.
    public static void main(String[] args) {
        launch();
    }
}
