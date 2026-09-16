package com.travis.customclicker.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MainController {

    // Clicker controls
    @FXML private Spinner<Integer> fixedValueSpinner, minIntervalSpinner, maxIntervalSpinner, xSpinner, ySpinner;
    @FXML private Spinner<Double> minCpsSpinner, maxCpsSpinner;
    @FXML private ComboBox<String> mouseButtonCombo, clickTypeCombo, repeatCombo, profileCombo;
    @FXML private ToggleButton fixedTimingButton, randomCpsButton, randomIntervalButton;
    @FXML private ToggleButton fixedIntervalButton, fixedCpsButton;
    @FXML private Label fixedValueLabel, fixedHelperLabel;
    @FXML private Label fixedEquivalentTitle, fixedEquivalentValue, fixedEquivalentHelper;
    @FXML private GridPane fixedPane, randomCpsPane, randomIntervalPane;

    // Navigation
    @FXML private Button clickerNavButton, profilesNavButton;
    @FXML private VBox clickerPage, profilesPage;

    // Profiles
    @FXML private VBox profileList;
    @FXML private TextField profileNameField, profileHotkeyField;
    @FXML private TextArea profileDescriptionField;
    @FXML private Label profileCountLabel;
    @FXML private Button deleteProfileButton;

    private final List<Profile> profiles = new ArrayList<>();
    private Profile selectedProfile;
    private int nextProfileNumber = 1;

    private double xOffset, yOffset;

    private static class Profile {
        String name, description, hotkey;
        boolean isDefault;

        Profile(String name, String description, String hotkey, boolean isDefault) {
            this.name = name;
            this.description = description;
            this.hotkey = hotkey;
            this.isDefault = isDefault;
        }
    }

    @FXML
    private void initialize() {
        initializeClicker();
        initializeProfiles();
        showPage(clickerPage);
    }

    private void initializeClicker() {
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

        setupTab(fixedTimingButton, () -> showTimingPane(fixedPane));
        setupTab(randomCpsButton, () -> showTimingPane(randomCpsPane));
        setupTab(randomIntervalButton, () -> showTimingPane(randomIntervalPane));

        setupTab(fixedIntervalButton, this::showFixedIntervalMode);
        setupTab(fixedCpsButton, this::showFixedCpsMode);
    }

    private void initializeProfiles() {
        profiles.add(new Profile("Default", "Default profile for general use.", "", true));
        selectProfile(profiles.get(0));
    }

    private void setupCombo(ComboBox<String> combo, String selected, String... items) {
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
        configureFixedMode("Interval (ms)", "Use a constant delay between clicks.",
                "Equivalent Speed", "10 CPS", "Equivalent clicking speed.", 1, 60000, 100);
    }

    private void showFixedCpsMode() {
        configureFixedMode("Clicks per second (CPS)", "Use a constant number of clicks per second.",
                "Equivalent Interval", "100 ms", "Equivalent delay between clicks.", 1, 100, 10);
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

    // Navigation

    @FXML private void handleShowClicker() { showPage(clickerPage); }
    @FXML private void handleShowProfiles() { showPage(profilesPage); }

    private void showPage(VBox page) {
        clickerPage.setVisible(page == clickerPage);
        clickerPage.setManaged(page == clickerPage);

        profilesPage.setVisible(page == profilesPage);
        profilesPage.setManaged(page == profilesPage);

        clickerNavButton.getStyleClass().removeAll("nav-active", "nav-button");
        profilesNavButton.getStyleClass().removeAll("nav-active", "nav-button");

        clickerNavButton.getStyleClass().add(page == clickerPage ? "nav-active" : "nav-button");
        profilesNavButton.getStyleClass().add(page == profilesPage ? "nav-active" : "nav-button");
    }

    // Profile management (in-memory UI version)

    @FXML
    private void handleNewProfile() {
        Profile profile = new Profile("New Profile " + nextProfileNumber++, "", "", false);
        profiles.add(profile);
        selectProfile(profile);
        showPage(profilesPage);
    }

    private void selectProfile(Profile profile) {
        selectedProfile = profile;

        profileNameField.setText(profile.name);
        profileDescriptionField.setText(profile.description);
        profileHotkeyField.setText(profile.hotkey);

        deleteProfileButton.setDisable(profile.isDefault);
        refreshProfileList();
    }

    private void refreshProfileList() {
        profileList.getChildren().clear();

        for (Profile profile : profiles) {
            Label icon = new Label("✦");
            icon.getStyleClass().add("profile-item-icon");

            Label name = new Label(profile.name);
            name.getStyleClass().add("profile-item-title");

            Label details = new Label("Fixed  •  Single  •  Cursor");
            details.getStyleClass().add("muted");

            VBox text = new VBox(5, name, details);
            HBox.setHgrow(text, Priority.ALWAYS);

            Label star = new Label(profile.isDefault ? "★" : "");
            star.getStyleClass().add("profile-default-star");

            HBox content = new HBox(12, icon, text, star);
            content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Button card = new Button();
            card.setGraphic(content);
            card.setMaxWidth(Double.MAX_VALUE);
            card.getStyleClass().add("profile-list-item");

            if (profile == selectedProfile) card.getStyleClass().add("profile-list-item-selected");

            card.setOnAction(event -> selectProfile(profile));
            profileList.getChildren().add(card);
        }

        profileCountLabel.setText(profiles.size() + (profiles.size() == 1 ? " profile" : " profiles"));

        profileCombo.getItems().setAll(profiles.stream().map(p -> p.name).toList());
        if (selectedProfile != null) profileCombo.setValue(selectedProfile.name);
    }

    @FXML
    private void handleSaveProfile() {
        if (selectedProfile == null) return;

        String newName = profileNameField.getText().trim();

        if (newName.isEmpty()) {
            showAlert("Invalid name", "Please enter a profile name.");
            return;
        }

        boolean duplicate = profiles.stream()
                .anyMatch(p -> p != selectedProfile && p.name.equalsIgnoreCase(newName));

        if (duplicate) {
            showAlert("Duplicate name", "A profile with that name already exists.");
            return;
        }

        selectedProfile.name = newName;
        selectedProfile.description = profileDescriptionField.getText();
        selectedProfile.hotkey = profileHotkeyField.getText().trim();

        refreshProfileList();
    }

    @FXML
    private void handleDuplicateProfile() {
        if (selectedProfile == null) return;

        String baseName = selectedProfile.name + " Copy";
        String name = baseName;
        int number = 2;

        while (profileNameExists(name)) name = baseName + " " + number++;

        Profile copy = new Profile(name, selectedProfile.description, selectedProfile.hotkey, false);
        profiles.add(copy);
        selectProfile(copy);
    }

    private boolean profileNameExists(String name) {
        return profiles.stream().anyMatch(p -> p.name.equalsIgnoreCase(name));
    }

    @FXML
    private void handleSetDefault() {
        if (selectedProfile == null) return;

        profiles.forEach(p -> p.isDefault = false);
        selectedProfile.isDefault = true;
        selectProfile(selectedProfile);
    }

    @FXML
    private void handleDeleteProfile() {
        if (selectedProfile == null || selectedProfile.isDefault) return;

        profiles.remove(selectedProfile);
        selectProfile(profiles.stream().filter(p -> p.isDefault).findFirst().orElse(profiles.get(0)));
    }

    @FXML
    private void handleEditConfiguration() {
        showPage(clickerPage);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    // Existing application controls

    @FXML private void handleStart() { System.out.println("Start button clicked"); }
    @FXML private void handleMinimize(ActionEvent event) { getStage(event).setIconified(true); }
    @FXML private void handleClose(ActionEvent event) { getStage(event).close(); }

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