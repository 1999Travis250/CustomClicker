package com.travis.customclicker.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.paint.Color;

import com.travis.customclicker.model.ClickSettings;

import java.util.ArrayList;
import java.util.List;

public class MainController {

    // Clicker
    @FXML private Spinner<Integer> fixedValueSpinner, minCpsSpinner, maxCpsSpinner, minIntervalSpinner, maxIntervalSpinner, xSpinner, ySpinner, repeatAmountSpinner;
    @FXML private ComboBox<String> mouseButtonCombo, clickTypeCombo, repeatCombo, profileCombo;
    @FXML private ToggleButton fixedTimingButton, randomCpsButton, randomIntervalButton, fixedIntervalButton, fixedCpsButton;
    @FXML private Label fixedValueLabel, fixedHelperLabel, fixedEquivalentTitle, fixedEquivalentValue, fixedEquivalentHelper, randomCpsEquivalentValue, randomIntervalEquivalentValue;
    @FXML private GridPane fixedPane, randomCpsPane, randomIntervalPane;
    @FXML private RadioButton followCursorButton, fixedPositionButton;
    @FXML private HBox repeatAmountRow;

    // Navigation
    @FXML private Button clickerNavButton, profilesNavButton, settingsNavButton, aboutNavButton;
    @FXML private VBox clickerPage, profilesPage, settingsPage, aboutPage;

    // Profiles
    @FXML private VBox profileList;
    @FXML private TextField profileNameField, profileHotkeyField;
    @FXML private TextArea profileDescriptionField;
    @FXML private Label profileCountLabel;
    @FXML private Button deleteProfileButton;

    private final List<Profile> profiles = new ArrayList<>();
    private Profile selectedProfile;
    private int nextProfileNumber = 1;

    // Settings
    @FXML private ComboBox<String> languageCombo, uiScaleCombo, updateCombo;
    @FXML private ToggleButton greenAccent, blueAccent, purpleAccent, pinkAccent, orangeAccent, yellowAccent;
    @FXML private ToggleButton launchStartupCheck, alwaysOnTopCheck, minimizeToTrayCheck;
    @FXML private TextField startHotkeyField, nextProfileHotkeyField;

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
        initializeSettings();
        showPage(clickerPage);
    }

    // CLICKER

    private void initializeClicker() {
        showFixedIntervalMode();

        initIntSpinner(minCpsSpinner, 1, 1000, 5);
        initIntSpinner(maxCpsSpinner, 1, 1000, 10);
        initIntSpinner(minIntervalSpinner, 1, 300000, 100);
        initIntSpinner(maxIntervalSpinner, 1, 300000, 200);
        initIntSpinner(xSpinner, 0, 50000, 0);
        initIntSpinner(ySpinner, 0, 50000, 0);
        initIntSpinner(repeatAmountSpinner, 1, 100000000, 10);

        restrictNumericInput(fixedValueSpinner);
        restrictNumericInput(minCpsSpinner);
        restrictNumericInput(maxCpsSpinner);
        restrictNumericInput(minIntervalSpinner);
        restrictNumericInput(maxIntervalSpinner);
        restrictNumericInput(xSpinner);
        restrictNumericInput(ySpinner);
        restrictNumericInput(repeatAmountSpinner);

        setupCombo(mouseButtonCombo, "Left", "Left", "Right", "Middle");
        setupCombo(clickTypeCombo, "Single", "Single", "Double");
        setupCombo(repeatCombo, "Until stopped", "Until stopped", "Fixed amount");

        repeatCombo.valueProperty().addListener((obs, oldValue, value) -> {
            boolean fixedAmount = "Fixed amount".equals(value);
            repeatAmountRow.setVisible(fixedAmount);
            repeatAmountRow.setManaged(fixedAmount);
        });

        fixedPositionButton.selectedProperty().addListener((obs, oldValue, selected) -> {
            xSpinner.setDisable(!selected);
            ySpinner.setDisable(!selected);
        });

        xSpinner.setDisable(true);
        ySpinner.setDisable(true);

        setupTab(fixedTimingButton, () -> showTimingPane(fixedPane));
        setupTab(randomCpsButton, () -> showTimingPane(randomCpsPane));
        setupTab(randomIntervalButton, () -> showTimingPane(randomIntervalPane));
        setupTab(fixedIntervalButton, this::showFixedIntervalMode);
        setupTab(fixedCpsButton, this::showFixedCpsMode);

        setupTimingConversions();
    }

    private void initIntSpinner(Spinner<Integer> spinner, int min, int max, int initial) {
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initial));
    }

    private void restrictNumericInput(Spinner<Integer> spinner) {
        spinner.getEditor().setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d*") ? change : null
        ));
    }

    private void setupCombo(ComboBox<String> combo, String selected, String... items) {
        combo.getItems().setAll(items);
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

        if (fixedValueSpinner.getValueFactory() != null) updateFixedEquivalent();
    }

    private void showFixedCpsMode() {
        configureFixedMode("Clicks per second (CPS)", "Use a constant number of clicks per second.",
                "Equivalent Interval", "100 ms", "Equivalent delay between clicks.", 1, 100, 10);

        if (fixedValueSpinner.getValueFactory() != null) updateFixedEquivalent();
    }

    private void configureFixedMode(String label, String helper, String equivalentTitle,
                                    String equivalentValue, String equivalentHelper,
                                    int min, int max, int initial) {
        fixedValueLabel.setText(label);
        fixedHelperLabel.setText(helper);
        fixedEquivalentTitle.setText(equivalentTitle);
        fixedEquivalentValue.setText(equivalentValue);
        fixedEquivalentHelper.setText(equivalentHelper);
        initIntSpinner(fixedValueSpinner, min, max, initial);
    }

    private void setupTimingConversions() {
        fixedValueSpinner.valueProperty().addListener((obs, oldValue, newValue) -> updateFixedEquivalent());
        fixedValueSpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> updateFixedEquivalent());

        minCpsSpinner.valueProperty().addListener((obs, oldValue, newValue) -> updateRandomCpsEquivalent());
        maxCpsSpinner.valueProperty().addListener((obs, oldValue, newValue) -> updateRandomCpsEquivalent());
        minCpsSpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> updateRandomCpsEquivalent());
        maxCpsSpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> updateRandomCpsEquivalent());

        minIntervalSpinner.valueProperty().addListener((obs, oldValue, newValue) -> updateRandomIntervalEquivalent());
        maxIntervalSpinner.valueProperty().addListener((obs, oldValue, newValue) -> updateRandomIntervalEquivalent());
        minIntervalSpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> updateRandomIntervalEquivalent());
        maxIntervalSpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> updateRandomIntervalEquivalent());

        updateFixedEquivalent();
        updateRandomCpsEquivalent();
        updateRandomIntervalEquivalent();
    }

    private int getSpinnerInput(Spinner<Integer> spinner) {
        String text = spinner.getEditor().getText().trim();

        if (text.isEmpty()) return 0;

        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updateFixedEquivalent() {
        int value = getSpinnerInput(fixedValueSpinner);

        if (value <= 0) {
            fixedEquivalentValue.setText(fixedCpsButton.isSelected() ? "0 ms" : "0 CPS");
            return;
        }

        if (fixedCpsButton.isSelected()) {
            double interval = 1000.0 / value;
            fixedEquivalentValue.setText(formatNumber(interval) + " ms");
        } else {
            double cps = 1000.0 / value;
            fixedEquivalentValue.setText(formatNumber(cps) + " CPS");
        }
    }

    private void updateRandomCpsEquivalent() {
        int minCps = getSpinnerInput(minCpsSpinner);
        int maxCps = getSpinnerInput(maxCpsSpinner);

        if (minCps <= 0 || maxCps <= 0) {
            randomCpsEquivalentValue.setText("≈ 0 ms");
            return;
        }

        if (minCps > maxCps) {
            randomCpsEquivalentValue.setText("Invalid range");
            return;
        }

        double minInterval = 1000.0 / maxCps;
        double maxInterval = 1000.0 / minCps;

        randomCpsEquivalentValue.setText(
                "≈ " + formatNumber(minInterval) + " – " + formatNumber(maxInterval) + " ms"
        );
    }

    private void updateRandomIntervalEquivalent() {
        int minInterval = getSpinnerInput(minIntervalSpinner);
        int maxInterval = getSpinnerInput(maxIntervalSpinner);

        if (minInterval <= 0 || maxInterval <= 0) {
            randomIntervalEquivalentValue.setText("≈ 0 CPS");
            return;
        }

        if (minInterval > maxInterval) {
            randomIntervalEquivalentValue.setText("Invalid range");
            return;
        }

        double minCps = 1000.0 / maxInterval;
        double maxCps = 1000.0 / minInterval;

        randomIntervalEquivalentValue.setText(
                "≈ " + formatNumber(minCps) + " – " + formatNumber(maxCps) + " CPS"
        );
    }

    private String formatNumber(double value) {
        if (Math.abs(value - Math.round(value)) < 0.001)
            return String.valueOf(Math.round(value));

        return String.format("%.2f", value)
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "");
    }

    private ClickSettings readClickSettings() {
        ClickSettings settings = new ClickSettings();

        if (randomCpsButton.isSelected())
            settings.setTimingMode(ClickSettings.TimingMode.RANDOM_CPS);
        else if (randomIntervalButton.isSelected())
            settings.setTimingMode(ClickSettings.TimingMode.RANDOM_INTERVAL);
        else if (fixedCpsButton.isSelected())
            settings.setTimingMode(ClickSettings.TimingMode.FIXED_CPS);
        else
            settings.setTimingMode(ClickSettings.TimingMode.FIXED_INTERVAL);

        settings.setFixedValue(getSpinnerInput(fixedValueSpinner));
        settings.setMinCps(getSpinnerInput(minCpsSpinner));
        settings.setMaxCps(getSpinnerInput(maxCpsSpinner));
        settings.setMinInterval(getSpinnerInput(minIntervalSpinner));
        settings.setMaxInterval(getSpinnerInput(maxIntervalSpinner));

        settings.setMouseButton(switch (mouseButtonCombo.getValue()) {
            case "Right" -> ClickSettings.MouseButton.RIGHT;
            case "Middle" -> ClickSettings.MouseButton.MIDDLE;
            default -> ClickSettings.MouseButton.LEFT;
        });

        settings.setClickType(
                "Double".equals(clickTypeCombo.getValue())
                        ? ClickSettings.ClickType.DOUBLE
                        : ClickSettings.ClickType.SINGLE
        );

        settings.setRepeatMode(
                "Fixed amount".equals(repeatCombo.getValue())
                        ? ClickSettings.RepeatMode.FIXED_AMOUNT
                        : ClickSettings.RepeatMode.UNTIL_STOPPED
        );

        settings.setTargetMode(
                fixedPositionButton.isSelected()
                        ? ClickSettings.TargetMode.FIXED_POSITION
                        : ClickSettings.TargetMode.FOLLOW_CURSOR
        );

        settings.setX(getSpinnerInput(xSpinner));
        settings.setY(getSpinnerInput(ySpinner));
        settings.setRepeatAmount(getSpinnerInput(repeatAmountSpinner));

        return settings;
    }

    private boolean validateSettings(ClickSettings settings) {
        if (settings.getTimingMode() == ClickSettings.TimingMode.RANDOM_CPS &&
                settings.getMinCps() > settings.getMaxCps()) {
            showAlert("Invalid CPS range", "Minimum CPS cannot be greater than maximum CPS.");
            return false;
        }

        if (settings.getTimingMode() == ClickSettings.TimingMode.RANDOM_INTERVAL &&
                settings.getMinInterval() > settings.getMaxInterval()) {
            showAlert("Invalid interval range", "Minimum interval cannot be greater than maximum interval.");
            return false;
        }

        return true;
    }

    // NAVIGATION

    @FXML private void handleShowClicker() { showPage(clickerPage); }
    @FXML private void handleShowProfiles() { showPage(profilesPage); }
    @FXML private void handleShowSettings() { showPage(settingsPage); }
    @FXML private void handleShowAbout() { showPage(aboutPage); }

    private void showPage(VBox selectedPage) {
        setPage(clickerPage, clickerNavButton, selectedPage);
        setPage(profilesPage, profilesNavButton, selectedPage);
        setPage(settingsPage, settingsNavButton, selectedPage);
        setPage(aboutPage, aboutNavButton, selectedPage);
    }

    private void setPage(VBox page, Button button, VBox selectedPage) {
        boolean active = page == selectedPage;
        page.setVisible(active);
        page.setManaged(active);

        button.getStyleClass().removeAll("nav-active", "nav-button");
        button.getStyleClass().add(active ? "nav-active" : "nav-button");
    }

    // PROFILES

    private void initializeProfiles() {
        profiles.add(new Profile("Default", "Default profile for general use.", "", true));
        selectProfile(profiles.get(0));
    }

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

        for (Profile profile : profiles)
            profileList.getChildren().add(createProfileCard(profile));

        profileCountLabel.setText(profiles.size() + (profiles.size() == 1 ? " profile" : " profiles"));
        profileCombo.getItems().setAll(profiles.stream().map(p -> p.name).toList());

        if (selectedProfile != null) profileCombo.setValue(selectedProfile.name);
    }

    private Button createProfileCard(Profile profile) {
        FontIcon profileIcon = new FontIcon("fth-file-text");
        profileIcon.setIconSize(24);
        profileIcon.setIconColor(Color.web("#17dc7b"));

        StackPane icon = new StackPane(profileIcon);
        icon.getStyleClass().add("profile-item-icon");

        Label name = new Label(profile.name);
        name.getStyleClass().add("profile-item-title");

        Label details = new Label("Fixed  •  Single  •  Cursor");
        details.getStyleClass().add("muted");

        VBox text = new VBox(5, name, details);
        HBox.setHgrow(text, Priority.ALWAYS);

        FontIcon star = new FontIcon("fth-star");
        star.setIconSize(19);
        star.setIconColor(Color.web("#17dc7b"));
        star.setVisible(profile.isDefault);
        star.setManaged(profile.isDefault);

        HBox content = new HBox(12, icon, text, star);
        content.setAlignment(Pos.CENTER_LEFT);

        Button card = new Button();
        card.setGraphic(content);
        card.setMaxWidth(Double.MAX_VALUE);
        card.getStyleClass().add("profile-list-item");

        if (profile == selectedProfile) card.getStyleClass().add("profile-list-item-selected");

        card.setOnAction(event -> selectProfile(profile));
        return card;
    }

    @FXML
    private void handleSaveProfile() {
        if (selectedProfile == null) return;

        String name = profileNameField.getText().trim();

        if (name.isEmpty()) {
            showAlert("Invalid name", "Please enter a profile name.");
            return;
        }

        if (profileNameExists(name, selectedProfile)) {
            showAlert("Duplicate name", "A profile with that name already exists.");
            return;
        }

        selectedProfile.name = name;
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

        while (profileNameExists(name, null)) name = baseName + " " + number++;

        Profile copy = new Profile(name, selectedProfile.description, selectedProfile.hotkey, false);
        profiles.add(copy);
        selectProfile(copy);
    }

    private boolean profileNameExists(String name, Profile excluded) {
        return profiles.stream().anyMatch(p -> p != excluded && p.name.equalsIgnoreCase(name));
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

    @FXML private void handleEditConfiguration() { showPage(clickerPage); }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    // SETTINGS

    private void initializeSettings() {
        setupCombo(languageCombo, "English", "English");
        setupCombo(uiScaleCombo, "100% (Default)", "100% (Default)", "125%", "150%");
        setupCombo(updateCombo, "Automatically", "Automatically", "Manually", "Never");

        startHotkeyField.setText("F6");
        nextProfileHotkeyField.setText("F8");

        for (ToggleButton accent : new ToggleButton[]{
                greenAccent, blueAccent, purpleAccent,
                pinkAccent, orangeAccent, yellowAccent
        }) {
            setupTab(accent, () -> {});
        }

        alwaysOnTopCheck.selectedProperty().addListener((obs, oldValue, enabled) -> {
            Stage stage = (Stage) alwaysOnTopCheck.getScene().getWindow();
            stage.setAlwaysOnTop(enabled);
        });
    }

    // APPLICATION CONTROLS

    @FXML
    private void handleStart() {
        ClickSettings settings = readClickSettings();

        if (!validateSettings(settings)) return;

        System.out.println(settings);
    }

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