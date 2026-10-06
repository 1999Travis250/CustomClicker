package com.travis.customclicker.controller;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.travis.customclicker.model.ClickSettings;
import com.travis.customclicker.model.Profile;
import com.travis.customclicker.service.AutoClickService;
import com.travis.customclicker.service.GlobalHotkeyService;
import com.travis.customclicker.service.PersistenceService;
import com.travis.customclicker.model.AppData;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    /* Clicker */
    @FXML private Spinner<Integer> fixedValueSpinner, minCpsSpinner, maxCpsSpinner, minIntervalSpinner, maxIntervalSpinner, xSpinner, ySpinner, repeatAmountSpinner;
    @FXML private ComboBox<String> mouseButtonCombo, clickTypeCombo, repeatCombo, profileCombo;
    @FXML private ToggleButton fixedTimingButton, randomTimingButton, fixedIntervalButton, fixedCpsButton, randomCpsButton, randomIntervalButton;
    @FXML private Label fixedValueLabel, fixedHelperLabel, fixedEquivalentTitle, fixedEquivalentValue, fixedEquivalentHelper, randomCpsEquivalentValue, randomIntervalEquivalentValue;
    @FXML private GridPane fixedPane, randomPane;
    @FXML private VBox randomCpsInputPane, randomIntervalInputPane, randomCpsEquivalentPane, randomIntervalEquivalentPane;
    @FXML private RadioButton followCursorButton, fixedPositionButton;
    @FXML private HBox repeatAmountRow, statusPill;
    @FXML private Button startButton;
    @FXML private Label startButtonText, statusText, startHotkeyBadge;
    @FXML private Circle statusDot;
    @FXML private SVGPath startIcon;

    private AutoClickService autoClickService;
    private GlobalHotkeyService globalHotkeyService;
    private boolean clickerRunning = false, pickingPosition = false;
    private FadeTransition statusPulse;
    private String previousStartHotkey = "F6", previousNextProfileHotkey = "F7";
    private int startHotkeyCode = NativeKeyEvent.VC_F6;
    private int nextProfileHotkeyCode = NativeKeyEvent.VC_F7;
    private PersistenceService persistenceService;
    private static final double BASE_WIDTH = 1200;
    private static final double BASE_HEIGHT = 800;

    /* Navigation */
    @FXML private Button clickerNavButton, profilesNavButton, settingsNavButton, aboutNavButton;
    @FXML private VBox clickerPage, profilesPage, settingsPage, aboutPage;

    /* Profiles */
    @FXML private VBox profileList;
    @FXML private TextField profileNameField, profileHotkeyField;
    @FXML private TextArea profileDescriptionField;
    @FXML private Label profileCountLabel, profileTimingValue, profileBehaviorValue, profileTargetValue;
    @FXML private Button deleteProfileButton;
    private final List<Profile> profiles = new ArrayList<>();
    private Profile selectedProfile;
    private int nextProfileNumber = 1;

    /* Settings */
    @FXML private ComboBox<String> languageCombo, uiScaleCombo, updateCombo;
    @FXML private BorderPane appRoot;
    @FXML private ToggleButton greenAccent, blueAccent, purpleAccent, pinkAccent, orangeAccent, yellowAccent;
    @FXML private ToggleButton launchStartupCheck, alwaysOnTopCheck, minimizeToTrayCheck;
    @FXML private TextField startHotkeyField, nextProfileHotkeyField;
    private double xOffset, yOffset;
    private double uiScale = 1.0, normalScale = 1.0;
    private boolean normalFullscreen = false;
    private boolean windowSnapped = false;

    @FXML
    private void initialize() {
        initializeClicker();
        persistenceService = new PersistenceService();
        initializeProfiles();
        initializeSettings();
        showPage(clickerPage);
        try {
            autoClickService = new AutoClickService();
        } catch (Exception e) {
            showAlert("Click engine error", "Unable to initialize the mouse click engine.");
        }
        try {
            globalHotkeyService = new GlobalHotkeyService();
            globalHotkeyService.setHotkeyCode(startHotkeyCode);
            globalHotkeyService.setNextProfileHotkeyCode(nextProfileHotkeyCode);
            globalHotkeyService.start(
                    () -> Platform.runLater(() -> { if (!pickingPosition) handleStart(); }),
                    () -> Platform.runLater(() -> { if (!pickingPosition) selectNextProfile(); })
            );
        } catch (Exception e) {
            showAlert("Hotkey error", "Unable to initialize the global hotkey.");
        }
    }

    /* CLICKER */
    private void initializeClicker() {
        showFixedCpsMode();
        initIntSpinner(minCpsSpinner, 1, 1000, 5);
        initIntSpinner(maxCpsSpinner, 1, 1000, 10);
        initIntSpinner(minIntervalSpinner, 1, 300000, 100);
        initIntSpinner(maxIntervalSpinner, 1, 300000, 200);
        initIntSpinner(xSpinner, -50000, 50000, 0);
        initIntSpinner(ySpinner, -50000, 50000, 0);
        initIntSpinner(repeatAmountSpinner, 1, 100000000, 10);
        restrictNumericInput(fixedValueSpinner);
        restrictNumericInput(minCpsSpinner);
        restrictNumericInput(maxCpsSpinner);
        restrictNumericInput(minIntervalSpinner);
        restrictNumericInput(maxIntervalSpinner);
        restrictSignedNumericInput(xSpinner);
        restrictSignedNumericInput(ySpinner);
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
        setupTab(fixedTimingButton, () -> showTimingMode(true));
        setupTab(randomTimingButton, () -> showTimingMode(false));
        setupTab(fixedIntervalButton, this::showFixedIntervalMode);
        setupTab(fixedCpsButton, this::showFixedCpsMode);
        setupTab(randomCpsButton, this::showRandomCpsMode);
        setupTab(randomIntervalButton, this::showRandomIntervalMode);
        setupTimingConversions();
    }

    private void initIntSpinner(Spinner<Integer> spinner, int min, int max, int initial) {
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initial));
    }

    private void restrictNumericInput(Spinner<Integer> spinner) {
        spinner.getEditor().setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d*") ? change : null));
    }

    private void restrictSignedNumericInput(Spinner<Integer> spinner) {
        spinner.getEditor().setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("-?\\d*") ? change : null));
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

    private void showTimingMode(boolean fixed) {
        fixedPane.setVisible(fixed);
        fixedPane.setManaged(fixed);
        randomPane.setVisible(!fixed);
        randomPane.setManaged(!fixed);
    }

    private void showRandomCpsMode() {
        randomCpsInputPane.setVisible(true);
        randomCpsInputPane.setManaged(true);
        randomIntervalInputPane.setVisible(false);
        randomIntervalInputPane.setManaged(false);
        randomCpsEquivalentPane.setVisible(true);
        randomCpsEquivalentPane.setManaged(true);
        randomIntervalEquivalentPane.setVisible(false);
        randomIntervalEquivalentPane.setManaged(false);
    }

    private void showRandomIntervalMode() {
        randomCpsInputPane.setVisible(false);
        randomCpsInputPane.setManaged(false);
        randomIntervalInputPane.setVisible(true);
        randomIntervalInputPane.setManaged(true);
        randomCpsEquivalentPane.setVisible(false);
        randomCpsEquivalentPane.setManaged(false);
        randomIntervalEquivalentPane.setVisible(true);
        randomIntervalEquivalentPane.setManaged(true);
    }

    private void showFixedIntervalMode() {
        configureFixedMode("Interval (ms)", "Use a constant delay between clicks.",
                "Equivalent Speed", "10 CPS", "Equivalent clicking speed.", 1, 300000, 100);
        if (fixedValueSpinner.getValueFactory() != null) updateFixedEquivalent();
    }

    private void showFixedCpsMode() {
        configureFixedMode("Clicks per second (CPS)", "Use a constant number of clicks per second.",
                "Equivalent Interval", "100 ms", "Equivalent delay between clicks.", 1, 1000, 10);
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
        try { return Integer.parseInt(text); }
        catch (NumberFormatException e) { return 0; }
    }

    private void updateFixedEquivalent() {
        int value = getSpinnerInput(fixedValueSpinner);
        if (value <= 0) {
            fixedEquivalentValue.setText(fixedCpsButton.isSelected() ? "0 ms" : "0 CPS");
            return;
        }
        if (fixedCpsButton.isSelected())
            fixedEquivalentValue.setText(formatNumber(1000.0 / value) + " ms");
        else
            fixedEquivalentValue.setText(formatNumber(1000.0 / value) + " CPS");
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
        randomCpsEquivalentValue.setText(
                "≈ " + formatNumber(1000.0 / maxCps) + " – " +
                        formatNumber(1000.0 / minCps) + " ms");
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
        randomIntervalEquivalentValue.setText(
                "≈ " + formatNumber(1000.0 / maxInterval) + " – " +
                        formatNumber(1000.0 / minInterval) + " CPS");
    }

    private String formatNumber(double value) {
        if (Math.abs(value - Math.round(value)) < 0.001) return String.valueOf(Math.round(value));
        return String.format("%.2f", value).replaceAll("0+$", "").replaceAll("\\\\.$", "");
    }

    private ClickSettings readClickSettings() {
        ClickSettings settings = new ClickSettings();
        if (randomTimingButton.isSelected()) {
            settings.setTimingMode(randomIntervalButton.isSelected()
                    ? ClickSettings.TimingMode.RANDOM_INTERVAL
                    : ClickSettings.TimingMode.RANDOM_CPS);
        } else {
            settings.setTimingMode(fixedCpsButton.isSelected()
                    ? ClickSettings.TimingMode.FIXED_CPS
                    : ClickSettings.TimingMode.FIXED_INTERVAL);
        }

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

        settings.setClickType("Double".equals(clickTypeCombo.getValue())
                ? ClickSettings.ClickType.DOUBLE
                : ClickSettings.ClickType.SINGLE);
        settings.setRepeatMode("Fixed amount".equals(repeatCombo.getValue())
                ? ClickSettings.RepeatMode.FIXED_AMOUNT
                : ClickSettings.RepeatMode.UNTIL_STOPPED);
        settings.setTargetMode(fixedPositionButton.isSelected()
                ? ClickSettings.TargetMode.FIXED_POSITION
                : ClickSettings.TargetMode.FOLLOW_CURSOR);
        settings.setX(getSpinnerInput(xSpinner));
        settings.setY(getSpinnerInput(ySpinner));
        settings.setRepeatAmount(getSpinnerInput(repeatAmountSpinner));
        return settings;
    }

    private void loadClickSettings(ClickSettings settings) {
        if (settings == null) return;
        switch (settings.getTimingMode()) {
            case FIXED_CPS -> {
                fixedTimingButton.setSelected(true);
                fixedCpsButton.setSelected(true);
                showTimingMode(true);
                showFixedCpsMode();
                fixedValueSpinner.getValueFactory().setValue(settings.getFixedValue());
            }
            case FIXED_INTERVAL -> {
                fixedTimingButton.setSelected(true);
                fixedIntervalButton.setSelected(true);
                showTimingMode(true);
                showFixedIntervalMode();
                fixedValueSpinner.getValueFactory().setValue(settings.getFixedValue());
            }
            case RANDOM_CPS -> {
                randomTimingButton.setSelected(true);
                randomCpsButton.setSelected(true);
                showTimingMode(false);
                showRandomCpsMode();
            }
            case RANDOM_INTERVAL -> {
                randomTimingButton.setSelected(true);
                randomIntervalButton.setSelected(true);
                showTimingMode(false);
                showRandomIntervalMode();
            }
        }

        minCpsSpinner.getValueFactory().setValue((int) Math.round(settings.getMinCps()));
        maxCpsSpinner.getValueFactory().setValue((int) Math.round(settings.getMaxCps()));
        minIntervalSpinner.getValueFactory().setValue(settings.getMinInterval());
        maxIntervalSpinner.getValueFactory().setValue(settings.getMaxInterval());
        mouseButtonCombo.setValue(switch (settings.getMouseButton()) {
            case RIGHT -> "Right";
            case MIDDLE -> "Middle";
            default -> "Left";
        });

        clickTypeCombo.setValue(settings.getClickType() == ClickSettings.ClickType.DOUBLE ? "Double" : "Single");
        repeatCombo.setValue(settings.getRepeatMode() == ClickSettings.RepeatMode.FIXED_AMOUNT ? "Fixed amount" : "Until stopped");
        repeatAmountSpinner.getValueFactory().setValue(settings.getRepeatAmount());
        if (settings.getTargetMode() == ClickSettings.TargetMode.FIXED_POSITION)
            fixedPositionButton.setSelected(true);
        else
            followCursorButton.setSelected(true);
        xSpinner.getValueFactory().setValue(settings.getX());
        ySpinner.getValueFactory().setValue(settings.getY());
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

    @FXML
    private void handlePickFromScreen() {
        if (pickingPosition || clickerRunning) return;
        pickingPosition = true;
        Stage mainStage = (Stage) xSpinner.getScene().getWindow();
        Rectangle2D virtualBounds = getVirtualScreenBounds();
        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.20);");
        Scene pickerScene = new Scene(overlay, virtualBounds.getWidth(), virtualBounds.getHeight());
        pickerScene.setFill(Color.TRANSPARENT);
        pickerScene.setCursor(Cursor.CROSSHAIR);
        Stage pickerStage = new Stage();
        pickerStage.initStyle(StageStyle.TRANSPARENT);
        pickerStage.setAlwaysOnTop(true);
        pickerStage.setX(virtualBounds.getMinX());
        pickerStage.setY(virtualBounds.getMinY());
        pickerStage.setWidth(virtualBounds.getWidth());
        pickerStage.setHeight(virtualBounds.getHeight());
        pickerStage.setScene(pickerScene);
        Runnable cancelPicker = () -> {
            pickingPosition = false;
            pickerStage.close();
            mainStage.show();
            mainStage.toFront();
        };

        overlay.setOnMouseClicked(event -> {
            int x = (int) Math.round(event.getScreenX());
            int y = (int) Math.round(event.getScreenY());
            xSpinner.getValueFactory().setValue(x);
            ySpinner.getValueFactory().setValue(y);
            fixedPositionButton.setSelected(true);
            xSpinner.setDisable(false);
            ySpinner.setDisable(false);
            pickingPosition = false;
            pickerStage.close();
            mainStage.show();
            mainStage.toFront();
        });

        pickerScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) cancelPicker.run();
        });

        mainStage.hide();
        pickerStage.show();
        pickerStage.requestFocus();
    }

    private Rectangle2D getVirtualScreenBounds() {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (Screen screen : Screen.getScreens()) {
            Rectangle2D bounds = screen.getBounds();
            minX = Math.min(minX, bounds.getMinX());
            minY = Math.min(minY, bounds.getMinY());
            maxX = Math.max(maxX, bounds.getMaxX());
            maxY = Math.max(maxY, bounds.getMaxY());
        }
        return new Rectangle2D(minX, minY, maxX - minX, maxY - minY);
    }

    /* NAVIGATION */
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

    /* PROFILES */
    private void initializeProfiles() {
        AppData data;
        try {
            data = persistenceService.loadData();
        } catch (Exception e) {
            System.err.println("Unable to load app data: " + e.getMessage());
            data = new AppData();
        }
        if (data.getStartHotkeyCode() != null)
            startHotkeyCode = data.getStartHotkeyCode();
        if (data.getNextProfileHotkeyCode() != null)
            nextProfileHotkeyCode = data.getNextProfileHotkeyCode();
        if (data.getProfiles() != null)
            profiles.addAll(data.getProfiles());
        if (profiles.isEmpty()) {
            profiles.add(new Profile("Default", "Default profile for general use.", "", true, readClickSettings()));
        }
        profileCombo.setOnAction(event -> {
            String selectedName = profileCombo.getValue();
            if (selectedName == null || selectedProfile == null) return;
            if (selectedName.equals(selectedProfile.getName())) return;
            profiles.stream()
                    .filter(profile -> profile.getName().equals(selectedName))
                    .findFirst()
                    .ifPresent(this::selectProfile);
        });

        Profile profileToSelect = null;
        String selectedProfileName = data.getSelectedProfileName();
        if (selectedProfileName != null) {
            profileToSelect = profiles.stream()
                    .filter(profile -> profile.getName().equals(selectedProfileName))
                    .findFirst()
                    .orElse(null);
        }
        if (profileToSelect == null) {
            profileToSelect = profiles.stream()
                    .filter(Profile::isDefault)
                    .findFirst()
                    .orElse(profiles.get(0));
        }
        selectProfile(profileToSelect);
    }

    @FXML
    private void handleNewProfile() {
        Profile profile = new Profile("New Profile " + nextProfileNumber++, "", "", false, readClickSettings());
        profiles.add(profile);
        selectProfile(profile);
        saveProfiles();
        showPage(profilesPage);
    }

    private void selectProfile(Profile profile) {
        selectedProfile = profile;
        profileNameField.setText(profile.getName());
        profileDescriptionField.setText(profile.getDescription());
        profileHotkeyField.setText(profile.getHotkey());
        deleteProfileButton.setDisable(profile.isDefault());
        loadClickSettings(profile.getSettings());
        updateProfileDetails(profile);
        refreshProfileList();
        saveProfiles();
    }

    private void selectNextProfile() {
        if (profiles.isEmpty()) return;
        int currentIndex = profiles.indexOf(selectedProfile);
        int nextIndex = (currentIndex + 1) % profiles.size();
        selectProfile(profiles.get(nextIndex));
    }

    private String getProfileCardSummary(ClickSettings settings) {
        String timing = switch (settings.getTimingMode()) {
            case FIXED_CPS -> "Fixed CPS";
            case FIXED_INTERVAL -> "Fixed Interval";
            case RANDOM_CPS -> "Random CPS";
            case RANDOM_INTERVAL -> "Random Interval";
        };
        String clickType = settings.getClickType() == ClickSettings.ClickType.DOUBLE ? "Double" : "Single";
        String target = settings.getTargetMode() == ClickSettings.TargetMode.FIXED_POSITION ? "Fixed" : "Cursor";
        return timing + "  •  " + clickType + "  •  " + target;
    }

    private String getTimingSummary(ClickSettings settings) {
        return switch (settings.getTimingMode()) {
            case FIXED_CPS -> "Fixed  •  " + settings.getFixedValue() + " CPS";
            case FIXED_INTERVAL -> "Fixed  •  " + settings.getFixedValue() + " ms";
            case RANDOM_CPS -> "Random  •  " + formatNumber(settings.getMinCps()) +
                    " – " + formatNumber(settings.getMaxCps()) + " CPS";
            case RANDOM_INTERVAL -> "Random  •  " + settings.getMinInterval() +
                    " – " + settings.getMaxInterval() + " ms";
        };
    }

    private String getBehaviorSummary(ClickSettings settings) {
        String button = switch (settings.getMouseButton()) {
            case RIGHT -> "Right";
            case MIDDLE -> "Middle";
            default -> "Left";
        };
        String clickType = settings.getClickType() == ClickSettings.ClickType.DOUBLE ? "Double" : "Single";
        String repeat = settings.getRepeatMode() == ClickSettings.RepeatMode.FIXED_AMOUNT
                ? settings.getRepeatAmount() + " clicks"
                : "Until stopped";
        return button + "  •  " + clickType + "  •  " + repeat;
    }

    private String getTargetSummary(ClickSettings settings) {
        if (settings.getTargetMode() == ClickSettings.TargetMode.FIXED_POSITION)
            return "Fixed Position  •  X: " + settings.getX() + "  •  Y: " + settings.getY();
        return "Follow Cursor";
    }

    private void updateProfileDetails(Profile profile) {
        if (profile == null || profile.getSettings() == null) return;
        profileTimingValue.setText(getTimingSummary(profile.getSettings()));
        profileBehaviorValue.setText(getBehaviorSummary(profile.getSettings()));
        profileTargetValue.setText(getTargetSummary(profile.getSettings()));
    }

    private void refreshProfileList() {
        profileList.getChildren().clear();
        for (Profile profile : profiles)
            profileList.getChildren().add(createProfileCard(profile));
        profileCountLabel.setText(profiles.size() + (profiles.size() == 1 ? " profile" : " profiles"));
        profileCombo.getItems().setAll(profiles.stream().map(Profile::getName).toList());
        if (selectedProfile != null)
            profileCombo.setValue(selectedProfile.getName());
    }

    private Button createProfileCard(Profile profile) {
        FontIcon profileIcon = new FontIcon("fth-file-text");
        profileIcon.setIconSize(24);
        StackPane icon = new StackPane(profileIcon);
        icon.getStyleClass().add("profile-item-icon");
        Label name = new Label(profile.getName());
        name.getStyleClass().add("profile-item-title");
        Label details = new Label(getProfileCardSummary(profile.getSettings()));
        details.getStyleClass().add("muted");
        VBox text = new VBox(5, name, details);
        HBox.setHgrow(text, Priority.ALWAYS);
        FontIcon star = new FontIcon("fth-star");
        star.setIconSize(19);
        star.setVisible(profile.isDefault());
        star.setManaged(profile.isDefault());
        star.getStyleClass().add("profile-default-star");
        HBox content = new HBox(12, icon, text, star);
        content.setAlignment(Pos.CENTER_LEFT);
        Button card = new Button();
        card.setGraphic(content);
        card.setMaxWidth(Double.MAX_VALUE);
        card.getStyleClass().add("profile-list-item");
        if (profile == selectedProfile)
            card.getStyleClass().add("profile-list-item-selected");
        card.setOnAction(event -> selectProfile(profile));
        return card;
    }

    private void saveProfiles() {
        try {
            String selectedName = selectedProfile != null ? selectedProfile.getName() : null;
            persistenceService.saveData(new AppData(profiles, selectedName, startHotkeyCode, nextProfileHotkeyCode));
        } catch (Exception e) {
            showAlert("Save error", "Unable to save application data.");
            System.err.println("Unable to save application data: " + e.getMessage());
        }
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
        selectedProfile.setName(name);
        selectedProfile.setDescription(profileDescriptionField.getText());
        selectedProfile.setHotkey(profileHotkeyField.getText().trim());
        selectedProfile.setSettings(readClickSettings());
        updateProfileDetails(selectedProfile);
        refreshProfileList();
        saveProfiles();
    }

    @FXML
    private void handleDuplicateProfile() {
        if (selectedProfile == null) return;
        String baseName = selectedProfile.getName() + " Copy";
        String name = baseName;
        int number = 2;
        while (profileNameExists(name, null))
            name = baseName + " " + number++;
        Profile copy = new Profile(
                name,
                selectedProfile.getDescription(),
                selectedProfile.getHotkey(),
                false,
                selectedProfile.getSettings().copy()
        );
        profiles.add(copy);
        selectProfile(copy);
        saveProfiles();
    }

    private boolean profileNameExists(String name, Profile excluded) {
        return profiles.stream().anyMatch(profile ->
                profile != excluded && profile.getName().equalsIgnoreCase(name));
    }

    @FXML
    private void handleSetDefault() {
        if (selectedProfile == null) return;
        profiles.forEach(profile -> profile.setDefault(false));
        selectedProfile.setDefault(true);
        selectProfile(selectedProfile);
        saveProfiles();
    }

    @FXML
    private void handleDeleteProfile() {
        if (selectedProfile == null || selectedProfile.isDefault()) return;
        profiles.remove(selectedProfile);
        selectProfile(profiles.stream()
                .filter(Profile::isDefault)
                .findFirst()
                .orElse(profiles.get(0)));
        saveProfiles();
    }

    @FXML private void handleEditConfiguration() { showPage(clickerPage); }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    /* SETTINGS */
    private void initializeSettings() {
        setupCombo(languageCombo, "English", "English");
        setupCombo(uiScaleCombo, "100% (Default)", "75%", "100% (Default)", "Fill Window");
        uiScaleCombo.setOnAction(event -> {
            String value = uiScaleCombo.getValue();

            if ("75%".equals(value))
                applyUiScale(0.75, false);
            else if ("Fill Window".equals(value))
                applyUiScale(1.0, true);
            else
                applyUiScale(1.0, false);
        });
        setupCombo(updateCombo, "Automatically", "Automatically", "Manually", "Never");
        startHotkeyField.setText(NativeKeyEvent.getKeyText(startHotkeyCode));
        nextProfileHotkeyField.setText(NativeKeyEvent.getKeyText(nextProfileHotkeyCode));
        startHotkeyField.setEditable(false);
        startHotkeyField.setText(NativeKeyEvent.getKeyText(startHotkeyCode));
        nextProfileHotkeyField.setText(NativeKeyEvent.getKeyText(nextProfileHotkeyCode));
        nextProfileHotkeyField.setEditable(false);
        startHotkeyField.setOnMouseClicked(event -> beginStartHotkeyCapture());
        nextProfileHotkeyField.setOnMouseClicked(event -> beginNextProfileHotkeyCapture());
        setupTab(greenAccent, () -> applyAccent("green"));
        setupTab(blueAccent, () -> applyAccent("blue"));
        setupTab(purpleAccent, () -> applyAccent("purple"));
        setupTab(pinkAccent, () -> applyAccent("pink"));
        setupTab(orangeAccent, () -> applyAccent("orange"));
        setupTab(yellowAccent, () -> applyAccent("yellow"));
        applyAccent("green");
        alwaysOnTopCheck.selectedProperty().addListener((obs, oldValue, enabled) -> {
            Stage stage = (Stage) alwaysOnTopCheck.getScene().getWindow();
            stage.setAlwaysOnTop(enabled);
        });
    }

    public void applyInitialUiScale() {
        String value = uiScaleCombo.getValue();

        if ("75%".equals(value))
            applyUiScale(0.75, false);
        else if ("Fill Window".equals(value))
            applyUiScale(1.0, true);
        else
            applyUiScale(1.0, false);
    }

    private void applyUiScale(double scale, boolean fullscreen) {
        Stage stage = (Stage) appRoot.getScene().getWindow();
        Parent content = appRoot.getParent();

        if (fullscreen) {
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            double scaleX = bounds.getWidth() / BASE_WIDTH;
            double scaleY = bounds.getHeight() / BASE_HEIGHT;

            content.getTransforms().setAll(new Scale(scaleX, scaleY));
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
        } else {
            content.getTransforms().setAll(new Scale(scale, scale));
            stage.setWidth(BASE_WIDTH * scale);
            stage.setHeight(BASE_HEIGHT * scale);
            stage.centerOnScreen();
        }
        uiScale = scale;
    }

    private void applyAccent(String accent) {
        String color = switch (accent) {
            case "blue" -> "#3688f5";
            case "purple" -> "#a14de0";
            case "pink" -> "#f04385";
            case "orange" -> "#ff863a";
            case "yellow" -> "#ffcd36";
            default -> "#16e889";
        };
        appRoot.setStyle("-fx-accent: " + color + ";");
    }

    private boolean isFunctionKey(int keyCode) {
        return keyCode >= NativeKeyEvent.VC_F1 && keyCode <= NativeKeyEvent.VC_F12;
    }

    private void beginStartHotkeyCapture() {
        if (globalHotkeyService == null) return;
        previousStartHotkey = startHotkeyField.getText();
        startHotkeyField.setText("Press a key (F1-F12)...");
        startHotkeyField.requestFocus();
        globalHotkeyService.captureNextKey(keyCode -> Platform.runLater(() -> {
            if (keyCode == NativeKeyEvent.VC_ESCAPE) {
                startHotkeyField.setText(previousStartHotkey);
                return;
            }
            if (!isFunctionKey(keyCode)) {
                startHotkeyField.setText(previousStartHotkey);
                showAlert("Invalid hotkey", "Please choose a function key between F1 and F12.");
                return;
            }
            if (keyCode == globalHotkeyService.getNextProfileHotkeyCode()) {
                startHotkeyField.setText(previousStartHotkey);
                showAlert("Hotkey already in use",
                        NativeKeyEvent.getKeyText(keyCode) + " is already assigned to Next Profile.");
                return;
            }
            String keyName = NativeKeyEvent.getKeyText(keyCode);
            globalHotkeyService.setHotkeyCode(keyCode);
            startHotkeyCode = keyCode;
            startHotkeyField.setText(keyName);
            startHotkeyBadge.setText(keyName);
            saveProfiles();
        }));
    }

    private void beginNextProfileHotkeyCapture() {
        if (globalHotkeyService == null) return;
        previousNextProfileHotkey = nextProfileHotkeyField.getText();
        nextProfileHotkeyField.setText("Press a key (F1-F12)...");
        nextProfileHotkeyField.requestFocus();
        globalHotkeyService.captureNextKey(keyCode -> Platform.runLater(() -> {
            if (keyCode == NativeKeyEvent.VC_ESCAPE) {
                nextProfileHotkeyField.setText(previousNextProfileHotkey);
                return;
            }
            if (!isFunctionKey(keyCode)) {
                nextProfileHotkeyField.setText(previousNextProfileHotkey);
                showAlert("Invalid hotkey", "Please choose a function key between F1 and F12.");
                return;
            }
            if (keyCode == globalHotkeyService.getHotkeyCode()) {
                nextProfileHotkeyField.setText(previousNextProfileHotkey);
                showAlert("Hotkey already in use",
                        NativeKeyEvent.getKeyText(keyCode) + " is already assigned to Start / Stop.");
                return;
            }
            String keyName = NativeKeyEvent.getKeyText(keyCode);
            globalHotkeyService.setNextProfileHotkeyCode(keyCode);
            nextProfileHotkeyCode = keyCode;
            nextProfileHotkeyField.setText(keyName);
            saveProfiles();
        }));
    }

    /* APPLICATION CONTROLS */
    @FXML
    private void handleStart() {
        if (autoClickService == null) {
            showAlert("Click engine error", "The click engine is not available.");
            return;
        }
        if (clickerRunning) {
            autoClickService.stop();
            return;
        }
        ClickSettings settings = readClickSettings();
        if (!validateSettings(settings)) return;
        clickerRunning = true;
        updateClickerUi(true);
        autoClickService.start(settings, () -> Platform.runLater(() -> {
            clickerRunning = false;
            updateClickerUi(false);
        }));
    }

    private void updateClickerUi(boolean running) {
        if (running) {
            startButtonText.setText("Stop Clicking");
            startIcon.setContent("M6 6h12v12H6z");
            if (!startButton.getStyleClass().contains("start-button-running"))
                startButton.getStyleClass().add("start-button-running");
            statusText.setText("Clicking");
            if (!statusDot.getStyleClass().contains("status-dot-running"))
                statusDot.getStyleClass().add("status-dot-running");
            if (!statusPill.getStyleClass().contains("status-pill-running"))
                statusPill.getStyleClass().add("status-pill-running");
            startStatusPulse();
        } else {
            startButtonText.setText("Start Clicking");
            startIcon.setContent("M8 5v14l11-7z");
            startButton.getStyleClass().remove("start-button-running");
            statusText.setText("Idle");
            statusDot.getStyleClass().remove("status-dot-running");
            statusPill.getStyleClass().remove("status-pill-running");
            stopStatusPulse();
        }
    }

    private void startStatusPulse() {
        stopStatusPulse();
        statusPulse = new FadeTransition(Duration.millis(700), statusDot);
        statusPulse.setFromValue(1.0);
        statusPulse.setToValue(0.3);
        statusPulse.setCycleCount(FadeTransition.INDEFINITE);
        statusPulse.setAutoReverse(true);
        statusPulse.play();
    }

    private void stopStatusPulse() {
        if (statusPulse != null) {
            statusPulse.stop();
            statusPulse = null;
        }
        statusDot.setOpacity(1.0);
    }

    @FXML
    private void handleMinimize(ActionEvent event) {
        getStage(event).setIconified(true);
    }

    @FXML
    private void handleClose(ActionEvent event) {
        if (autoClickService != null) autoClickService.stop();
        if (globalHotkeyService != null) globalHotkeyService.stop();
        getStage(event).close();
    }

    @FXML
    private void handleWindowPressed(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        if (windowSnapped) {
            if (normalFullscreen)
                applyUiScale(1.0, true);
            else
                applyUiScale(normalScale, false);

            if (!normalFullscreen) {
                stage.setX(event.getScreenX() - stage.getWidth() / 2);
                stage.setY(event.getScreenY() - 20);
            }

            windowSnapped = false;
        }
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    private void handleWindowDragged(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        if (windowSnapped) return;

        double newX = event.getScreenX() - xOffset;
        double newY = event.getScreenY() - yOffset;

        if (event.getScreenY() <= 20) {
            normalScale = uiScale;
            normalFullscreen = "Fill Window".equals(uiScaleCombo.getValue());

            applyUiScale(1.0, true);
            windowSnapped = true;
            return;
        }
        stage.setX(newX);
        stage.setY(newY);
    }
    
    private Stage getStage(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }
}
