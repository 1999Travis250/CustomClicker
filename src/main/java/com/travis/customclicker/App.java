package com.travis.customclicker;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        Label label = new Label("CustomClicker");

        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 600, 400);

        stage.setTitle("CustomClicker");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}