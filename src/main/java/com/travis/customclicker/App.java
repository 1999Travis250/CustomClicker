package com.travis.customclicker;

import com.travis.customclicker.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class App extends Application {

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
        stage.show();

        MainController controller = loader.getController();
        controller.applyInitialUiScale();
        controller.applyInitialAlwaysOnTop();

        stage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch();
    }
}