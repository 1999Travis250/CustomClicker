package com.travis.customclicker.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class MainController {

    @FXML private Spinner<Integer> fixedValueSpinner, minIntervalSpinner, maxIntervalSpinner, xSpinner, ySpinner;
    @FXML private Spinner<Double> minCpsSpinner, maxCpsSpinner;

    @FXML private ComboBox<String> mouseButtonCombo, clickTypeCombo, repeatCombo, profileCombo;

    @FXML private ToggleButton fixedTimingButton, randomCpsButton, randomIntervalButton;
    @FXML private ToggleButton fixedIntervalButton, fixedCpsButton;

    @FXML private Label fixedValueLabel, fixedHelperLabel;
    @FXML private Label fixedEquivalentTitle, fixedEquivalentValue, fixedEquivalentHelper;

    @FXML private GridPane fixedPane, randomCpsPane, randomIntervalPane;

    private double xOffset, yOffset;

    @FXML
    private void initialize() {
        showFixedIntervalMode();

        minCpsSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 100, 0.5, 0.1));
        maxCpsSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 100, 1.5, 0.1));

        minIntervalSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60000, 100));
        maxIntervalSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60000, 300));

        xSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 0));
        ySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 0));

        setupCombo(mouseButtonCombo, "Left", "Left", "Right", "Middle");
        setupCombo(clickTypeCombo, "Single", "Single", "Double");
        setupCombo(repeatCombo, "Until stopped", "Until stopped", "Fixed amount");
        setupCombo(profileCombo, "Default", "Default");

        setupTab(fixedTimingButton, () -> showTimingPane(fixedPane));
        setupTab(randomCpsButton, () -> showTimingPane(randomCpsPane));
        setupTab(randomIntervalButton, () -> showTimingPane(randomIntervalPane));

        setupTab(fixedIntervalButton, this::showFixedIntervalMode);
        setupTab(fixedCpsButton, this::showFixedCpsMode);
    }

    @SafeVarargs
    private final void setupCombo(ComboBox<String> combo, String selected, String... items) {
        combo.getItems().addAll(items);
        combo.setValue(selected);
    }

    private void setupTab(ToggleButton button, Runnable action) {
        button.setOnAction(event -> {
            if (!button.isSelected()) {
                button.setSelected(true);
                return;
            }
            action.run();
        });
    }

    private void showTimingPane(GridPane selectedPane) {
        for (GridPane pane : new GridPane[]{fixedPane, randomCpsPane, randomIntervalPane}) {
            boolean active = pane == selectedPane;
            pane.setVisible(active);
            pane.setManaged(active);
        }
    }

    private void showFixedIntervalMode() {
        configureFixedMode(
                "Interval (ms)",
                "Use a constant delay between clicks.",
                "Equivalent Speed",
                "10 CPS",
                "Equivalent clicking speed.",
                1, 60000, 100
        );
    }

    private void showFixedCpsMode() {
        configureFixedMode(
                "Clicks per second (CPS)",
                "Use a constant number of clicks per second.",
                "Equivalent Interval",
                "100 ms",
                "Equivalent delay between clicks.",
                1, 100, 10
        );
    }

    private void configureFixedMode(String valueLabel, String helper, String equivalentTitle,
                                    String equivalentValue, String equivalentHelper,
                                    int min, int max, int initial) {
        fixedValueLabel.setText(valueLabel);
        fixedHelperLabel.setText(helper);
        fixedEquivalentTitle.setText(equivalentTitle);
        fixedEquivalentValue.setText(equivalentValue);
        fixedEquivalentHelper.setText(equivalentHelper);
        fixedValueSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initial));
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