package com.travis.customclicker;

import com.travis.customclicker.controller.MainController;
import com.travis.customclicker.service.PersistenceService;
import com.travis.customclicker.service.SingleInstanceService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.Objects;

public class App extends Application {
    private SingleInstanceService singleInstance;

    // Loads the existing JavaFX view and connects window and tray lifecycle actions.
    @Override
    public void start(Stage stage) throws Exception {
        singleInstance = new SingleInstanceService(
                new PersistenceService().getDataFile().getParent());
        try {
            if (!singleInstance.start(() -> Platform.runLater(() -> {
                stage.show();
                stage.setIconified(false);
                stage.toFront();
                stage.requestFocus();
            }))) {
                Platform.exit();
                return;
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Unable to check whether CustomClicker is already running.\n\n" + e.getMessage());
            alert.setHeaderText(null);
            alert.setTitle("CustomClicker startup error");
            alert.showAndWait();
            Platform.exit();
            return;
        }
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
        controller.initializeUpdates(getHostServices()::showDocument);
    }

    @Override
    public void stop() throws Exception {
        if (singleInstance != null) singleInstance.close();
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
