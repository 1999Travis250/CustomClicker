package com.travis.customclicker.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class MainController {

    @FXML private Spinner<Integer> intervalSpinner;
    @FXML private Spinner<Integer> xSpinner;
    @FXML private Spinner<Integer> ySpinner;

    @FXML private ComboBox<String> mouseButtonCombo;
    @FXML private ComboBox<String> clickTypeCombo;
    @FXML private ComboBox<String> repeatCombo;
    @FXML private ComboBox<String> profileCombo;

    private double xOffset;
    private double yOffset;

    @FXML
    private void initialize() {
        intervalSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60000, 100)
        );

        xSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 0)
        );

        ySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 0)
        );

        mouseButtonCombo.getItems().addAll("Left", "Right", "Middle");
        mouseButtonCombo.setValue("Left");

        clickTypeCombo.getItems().addAll("Single", "Double");
        clickTypeCombo.setValue("Single");

        repeatCombo.getItems().addAll("Until stopped", "Fixed amount");
        repeatCombo.setValue("Until stopped");

        profileCombo.getItems().add("Default");
        profileCombo.setValue("Default");
    }

    @FXML
    private void handleStart() {
        System.out.println("Start button clicked");
    }

    @FXML
    private void handleMinimize(ActionEvent event) {
        getStage(event).setIconified(true);
    }

    @FXML
    private void handleClose(ActionEvent event) {
        getStage(event).close();
    }

    @FXML
    private void handleWindowPressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    private void handleWindowDragged(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

    private Stage getStage(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }
}