package com.travis.customclicker.controller;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.travis.customclicker.model.AppData;
import com.travis.customclicker.model.ClickSettings;
import com.travis.customclicker.model.Profile;
import com.travis.customclicker.service.AutoClickService;
import com.travis.customclicker.service.GlobalHotkeyService;
import com.travis.customclicker.service.PersistenceService;
import com.travis.customclicker.service.TrayService;
import com.travis.customclicker.service.WindowsStartupService;
import com.travis.customclicker.service.UpdateService;
import javafx.concurrent.Task;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.AWTException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MainController {

    private static final double BASE_WIDTH = 1200;
    private static final double BASE_HEIGHT = 800;

    /* Clicker */
    @FXML private Spinner<Integer> fixedValueSpinner;
    @FXML private Spinner<Integer> minCpsSpinner;
    @FXML private Spinner<Integer> maxCpsSpinner;
    @FXML private Spinner<Integer> minIntervalSpinner;
    @FXML private Spinner<Integer> maxIntervalSpinner;
    @FXML private Spinner<Integer> xSpinner;
    @FXML private Spinner<Integer> ySpinner;
    @FXML private Spinner<Integer> repeatAmountSpinner;
    @FXML private ComboBox<String> mouseButtonCombo;
    @FXML private ComboBox<String> clickTypeCombo;
    @FXML private ComboBox<String> repeatCombo;
    @FXML private ComboBox<String> profileCombo;
    @FXML private ToggleButton fixedTimingButton;
    @FXML private ToggleButton randomTimingButton;
    @FXML private ToggleButton fixedIntervalButton;
    @FXML private ToggleButton fixedCpsButton;
    @FXML private ToggleButton randomCpsButton;
    @FXML private ToggleButton randomIntervalButton;
    @FXML private Label fixedValueLabel;
    @FXML private Label fixedHelperLabel;
    @FXML private Label fixedEquivalentTitle;
    @FXML private Label fixedEquivalentValue;
    @FXML private Label fixedEquivalentHelper;
    @FXML private Label randomCpsEquivalentValue;
    @FXML private Label randomIntervalEquivalentValue;
    @FXML private GridPane fixedPane;
    @FXML private GridPane randomPane;
    @FXML private VBox randomCpsInputPane;
    @FXML private VBox randomIntervalInputPane;
    @FXML private VBox randomCpsEquivalentPane;
    @FXML private VBox randomIntervalEquivalentPane;
    @FXML private RadioButton followCursorButton;
    @FXML private RadioButton fixedPositionButton;
    @FXML private HBox repeatAmountRow;
    @FXML private HBox statusPill;
    @FXML private Button startButton;
    @FXML private Label startButtonText;
    @FXML private Label statusText;
    @FXML private Label startHotkeyBadge;
    @FXML private Label clickerSummary;
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

    /* Navigation */
    @FXML private Button clickerNavButton;
    @FXML private Button profilesNavButton;
    @FXML private Button settingsNavButton;
    @FXML private Button aboutNavButton;
    @FXML private VBox clickerPage;
    @FXML private VBox profilesPage;
    @FXML private VBox settingsPage;
    @FXML private VBox aboutPage;

    /* Profiles */
    @FXML private VBox profileList;
    @FXML private TextField profileNameField;
    @FXML private TextArea profileDescriptionField;
    @FXML private Label profileCountLabel;
    @FXML private Label profileTimingValue;
    @FXML private Label profileBehaviorValue;
    @FXML private Label profileTargetValue;
    @FXML private Button deleteProfileButton;
    private final List<Profile> profiles = new ArrayList<>();
    private Profile selectedProfile;
    private int nextProfileNumber = 1;

    /* Settings */
    @FXML private ComboBox<String> languageCombo;
    @FXML private ComboBox<String> uiScaleCombo;
    @FXML private ComboBox<String> updateCombo;
    @FXML private Button checkUpdatesButton;
    @FXML private Label versionLabel;
    private String savedUpdatePreference = "Automatically";
    private Consumer<String> openWebPage;
    private boolean checkingUpdates;
    private final UpdateService updateService = new UpdateService();
    @FXML private BorderPane appRoot;
    @FXML private ToggleButton greenAccent;
    @FXML private ToggleButton blueAccent;
    @FXML private ToggleButton purpleAccent;
    @FXML private ToggleButton pinkAccent;
    @FXML private ToggleButton orangeAccent;
    @FXML private ToggleButton yellowAccent;
    @FXML private ToggleButton cyanAccent;
    @FXML private ToggleButton launchStartupCheck;
    @FXML private ToggleButton alwaysOnTopCheck;
    @FXML private ToggleButton minimizeToTrayCheck;
    @FXML private TextField startHotkeyField;
    @FXML private TextField nextProfileHotkeyField;
    private double xOffset, yOffset;
    private double uiScale = 1.0, normalScale = 1.0;
    private boolean normalFullscreen = false;
    private boolean windowSnapped = false;
    private String lastNormalUiScale = "100% (Default)";
    private String savedThemeAccent = "green";
    private String savedUiScale = "100% (Default)";
    private boolean savedAlwaysOnTop = false;
    private boolean savedLaunchAtStartup = false;
    private boolean savedMinimizeToTray = false;
    private final TrayService trayService = new TrayService();
    private final WindowsStartupService startupService = new WindowsStartupService();

    // Initializes controls and saved state before starting the click and hotkey services.
    @FXML
    private void initialize() {
        initializeClicker();
        setupClickerSummary();
        persistenceService = new PersistenceService();
        initializeProfiles();
        initializeSettings();
        showPage(clickerPage);
        initializeClickEngine();
        initializeGlobalHotkeys();
    }

    // Creates the mouse engine and reports initialization failure through the existing dialog.
    private void initializeClickEngine() {
        try {
            autoClickService = new AutoClickService();
        } catch (Exception e) {
            showAlert("Click engine error", "Unable to initialize the mouse click engine.");
        }
    }

    // Starts the global hotkeys after their saved assignments have been restored.
    private void initializeGlobalHotkeys() {
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
    // Configures click inputs and their existing selection listeners.
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

    // Refreshes the click summary when an input changes.
    private void setupClickerSummary() {
        for (Spinner<Integer> spinner : List.of(fixedValueSpinner, minCpsSpinner,
                maxCpsSpinner, minIntervalSpinner, maxIntervalSpinner,
                xSpinner, ySpinner, repeatAmountSpinner)) {
            spinner.valueProperty().addListener((obs, oldValue, value) -> updateClickerSummary());
            spinner.getEditor().textProperty().addListener((obs, oldValue, value) -> updateClickerSummary());
        }

        for (ComboBox<String> combo : List.of(mouseButtonCombo, clickTypeCombo, repeatCombo)) {
            combo.valueProperty().addListener((obs, oldValue, value) -> updateClickerSummary());
        }

        followCursorButton.selectedProperty().addListener((obs, oldValue, value) -> updateClickerSummary());
        fixedPositionButton.selectedProperty().addListener((obs, oldValue, value) -> updateClickerSummary());
    }

    // Builds the displayed summary from the current input values.
    private void updateClickerSummary() {
        if (clickerSummary == null) return;

        ClickSettings settings = readClickSettings();

        String button = switch (settings.getMouseButton()) {
            case RIGHT -> "Right";
            case MIDDLE -> "Middle";
            default -> "Left";
        };

        String type = settings.getClickType() == ClickSettings.ClickType.DOUBLE
                ? "double click" : "click";

        String timing = switch (settings.getTimingMode()) {
            case FIXED_CPS -> "Fixed " + settings.getFixedValue() + " CPS";
            case FIXED_INTERVAL -> "Fixed " + settings.getFixedValue() + " ms";
            case RANDOM_CPS -> "Random " + formatNumber(settings.getMinCps()) + "–" +
                    formatNumber(settings.getMaxCps()) + " CPS";
            case RANDOM_INTERVAL -> "Random " + settings.getMinInterval() + "–" +
                    settings.getMaxInterval() + " ms";
        };

        String target = settings.getTargetMode() == ClickSettings.TargetMode.FIXED_POSITION
                ? "X: " + settings.getX() + ", Y: " + settings.getY()
                : "Follow cursor";

        String repeat = settings.getRepeatMode() == ClickSettings.RepeatMode.FIXED_AMOUNT
                ? settings.getRepeatAmount() + " clicks" : "Until stopped";

        clickerSummary.setText(button + " " + type + "  •  " + timing +
                "  •  " + target + "  •  " + repeat);
    }

    // Sets an integer spinner's limits and initial value.
    private void initIntSpinner(Spinner<Integer> spinner, int min, int max, int initial) {
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initial));
    }

    // Allows unsigned digits and temporarily empty editor text.
    private void restrictNumericInput(Spinner<Integer> spinner) {
        spinner.getEditor().setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d*") ? change : null));
    }

    // Allows signed digits and temporarily incomplete editor text.
    private void restrictSignedNumericInput(Spinner<Integer> spinner) {
        spinner.getEditor().setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("-?\\d*") ? change : null));
    }

    // Populates a combo box and selects its initial value.
    private void setupCombo(ComboBox<String> combo, String selected, String... items) {
        combo.getItems().setAll(items);
        combo.setValue(selected);
    }

    // Keeps a toggle selected and runs its action when activated.
    private void setupTab(ToggleButton button, Runnable action) {
        button.setOnAction(event -> {
            if (!button.isSelected()) {
                button.setSelected(true);
                return;
            }
            action.run();
            updateClickerSummary();
        });
    }

    // Shows the panels for fixed or random timing.
    private void showTimingMode(boolean fixed) {
        setVisibleAndManaged(fixedPane, fixed);
        setVisibleAndManaged(randomPane, !fixed);
    }

    // Shows the random CPS inputs and their equivalent interval.
    private void showRandomCpsMode() {
        showRandomMode(true);
    }

    // Shows the random interval inputs and their equivalent CPS.
    private void showRandomIntervalMode() {
        showRandomMode(false);
    }

    // Shows the input and equivalent-value panels for the selected random mode.
    private void showRandomMode(boolean cps) {
        setVisibleAndManaged(randomCpsInputPane, cps);
        setVisibleAndManaged(randomIntervalInputPane, !cps);
        setVisibleAndManaged(randomCpsEquivalentPane, cps);
        setVisibleAndManaged(randomIntervalEquivalentPane, !cps);
    }

    // Keeps layout participation consistent with a node's visibility.
    private void setVisibleAndManaged(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    // Configures fixed-interval labels and input limits.
    private void showFixedIntervalMode() {
        configureFixedMode("Interval (ms)", "Use a constant delay between clicks.",
                "Equivalent Speed", "10 CPS", "Equivalent clicking speed.", 1, 300000, 100);
        if (fixedValueSpinner.getValueFactory() != null) updateFixedEquivalent();
    }

    // Configures fixed-CPS labels and input limits.
    private void showFixedCpsMode() {
        configureFixedMode("Clicks per second (CPS)", "Use a constant number of clicks per second.",
                "Equivalent Interval", "100 ms", "Equivalent delay between clicks.", 1, 1000, 10);
        if (fixedValueSpinner.getValueFactory() != null) updateFixedEquivalent();
    }

    // Updates labels and resets the shared fixed-timing spinner.
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

    // Keeps timing equivalents synchronized with spinner input.
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

    // Reads editor text, returning zero for empty or invalid integers.
    private int getSpinnerInput(Spinner<Integer> spinner) {
        String text = spinner.getEditor().getText().trim();
        if (text.isEmpty()) return 0;
        try { return Integer.parseInt(text); }
        catch (NumberFormatException e) { return 0; }
    }

    // Displays the speed or interval equivalent of the fixed input.
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

    // Displays the interval range equivalent to the CPS inputs.
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

    // Displays the CPS range equivalent to the interval inputs.
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

    // Formats summary numbers using the existing precision and locale.
    private String formatNumber(double value) {
        if (Math.abs(value - Math.round(value)) < 0.001) return String.valueOf(Math.round(value));
        return String.format("%.2f", value).replaceAll("0+$", "").replaceAll("\\\\.$", "");
    }

    // Copies the current control values into independent click settings.
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

    // Applies a profile's click settings to the existing controls.
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

    // Rejects reversed random timing ranges before starting.
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

    // Opens the screen-position picker and supports cancellation with Escape.
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

    // Calculates the combined bounds of all connected displays.
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
    // Opens the clicker page.
    @FXML private void handleShowClicker() { showPage(clickerPage); }
    // Opens the profiles page.
    @FXML private void handleShowProfiles() { showPage(profilesPage); }
    // Opens the settings page.
    @FXML private void handleShowSettings() { showPage(settingsPage); }
    // Opens the about page.
    @FXML private void handleShowAbout() { showPage(aboutPage); }
    // Updates page visibility and the selected navigation button.
    private void showPage(VBox selectedPage) {
        setPage(clickerPage, clickerNavButton, selectedPage);
        setPage(profilesPage, profilesNavButton, selectedPage);
        setPage(settingsPage, settingsNavButton, selectedPage);
        setPage(aboutPage, aboutNavButton, selectedPage);
    }

    // Applies visibility and navigation styling for one page.
    private void setPage(VBox page, Button button, VBox selectedPage) {
        boolean active = page == selectedPage;
        setVisibleAndManaged(page, active);
        button.getStyleClass().removeAll("nav-active", "nav-button");
        button.getStyleClass().add(active ? "nav-active" : "nav-button");
    }

    /* PROFILES */
    // Loads saved application data and selects the saved or default profile.
    private void initializeProfiles() {
        AppData data;
        try {
            data = persistenceService.loadData();
        } catch (Exception e) {
            System.err.println("Unable to load app data: " + e.getMessage());
            data = new AppData();
        }
        if (data.getStartHotkeyCode() != null
                && GlobalHotkeyService.isFunctionKey(data.getStartHotkeyCode()))
            startHotkeyCode = data.getStartHotkeyCode();
        if (data.getNextProfileHotkeyCode() != null
                && GlobalHotkeyService.isFunctionKey(data.getNextProfileHotkeyCode()))
            nextProfileHotkeyCode = data.getNextProfileHotkeyCode();
        if (nextProfileHotkeyCode == startHotkeyCode)
            nextProfileHotkeyCode = startHotkeyCode == NativeKeyEvent.VC_F7
                    ? NativeKeyEvent.VC_F6 : NativeKeyEvent.VC_F7;
        if (data.getThemeAccent() != null)
            savedThemeAccent = data.getThemeAccent();
        if (data.getUiScale() != null)
            savedUiScale = data.getUiScale();
        savedAlwaysOnTop = data.isAlwaysOnTop();
        savedLaunchAtStartup = data.isLaunchAtStartup();
        savedMinimizeToTray = data.isMinimizeToTray();
        if (List.of("Automatically", "Manually", "Never").contains(
                data.getUpdatePreference() == null ? "" : data.getUpdatePreference()))
            savedUpdatePreference = data.getUpdatePreference();
        if (data.getProfiles() != null)
            profiles.addAll(data.getProfiles());
        if (profiles.isEmpty()) {
            profiles.add(new Profile("Default", "Default profile for general use.", true, readClickSettings()));
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
        updateClickerSummary();
    }

    // Creates a profile from the current click inputs.
    @FXML
    private void handleNewProfile() {
        Profile profile = new Profile("New Profile " + nextProfileNumber++, "", false, readClickSettings());
        profiles.add(profile);
        selectProfile(profile);
        saveApplicationData();
        updateClickerSummary();
        showPage(profilesPage);
    }

    // Loads a profile into the controls and persists the selection.
    private void selectProfile(Profile profile) {
        selectedProfile = profile;
        profileNameField.setText(profile.getName());
        profileDescriptionField.setText(profile.getDescription());
        deleteProfileButton.setDisable(profile.isDefault());
        loadClickSettings(profile.getSettings());
        updateProfileDetails(profile);
        refreshProfileList();
        saveApplicationData();
    }

    // Cycles through profiles in their existing list order.
    private void selectNextProfile() {
        if (profiles.isEmpty()) return;
        int currentIndex = profiles.indexOf(selectedProfile);
        int nextIndex = (currentIndex + 1) % profiles.size();
        selectProfile(profiles.get(nextIndex));
    }

    // Builds the compact summary displayed on a profile card.
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

    // Formats a profile's timing settings for its details panel.
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

    // Formats a profile's mouse button, click type, and repeat settings.
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

    // Formats a profile's cursor or fixed-position target.
    private String getTargetSummary(ClickSettings settings) {
        if (settings.getTargetMode() == ClickSettings.TargetMode.FIXED_POSITION)
            return "Fixed Position  •  X: " + settings.getX() + "  •  Y: " + settings.getY();
        return "Follow Cursor";
    }

    // Refreshes the selected profile's details labels.
    private void updateProfileDetails(Profile profile) {
        if (profile == null || profile.getSettings() == null) return;
        profileTimingValue.setText(getTimingSummary(profile.getSettings()));
        profileBehaviorValue.setText(getBehaviorSummary(profile.getSettings()));
        profileTargetValue.setText(getTargetSummary(profile.getSettings()));
    }

    // Rebuilds profile cards and synchronizes the profile selector.
    private void refreshProfileList() {
        profileList.getChildren().clear();
        for (Profile profile : profiles)
            profileList.getChildren().add(createProfileCard(profile));
        profileCountLabel.setText(profiles.size() + (profiles.size() == 1 ? " profile" : " profiles"));
        profileCombo.getItems().setAll(profiles.stream().map(Profile::getName).toList());
        if (selectedProfile != null)
            profileCombo.setValue(selectedProfile.getName());
    }

    // Creates a profile card with its existing styles and selection action.
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

    // Saves profiles, the current selection, hotkeys, and application settings.
    private void saveApplicationData() {
        try {
            String selectedName = selectedProfile != null ? selectedProfile.getName() : null;
            AppData data = new AppData(
                    profiles, selectedName, startHotkeyCode, nextProfileHotkeyCode,
                    savedThemeAccent, savedUiScale, savedAlwaysOnTop,
                    savedLaunchAtStartup, savedMinimizeToTray);
            data.setUpdatePreference(savedUpdatePreference);
            persistenceService.saveData(data);
        } catch (Exception e) {
            showAlert("Save error", "Unable to save application data.");
            System.err.println("Unable to save application data: " + e.getMessage());
        }
    }

    // Validates the name and saves edits to the selected profile.
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
        selectedProfile.setSettings(readClickSettings());
        updateProfileDetails(selectedProfile);
        refreshProfileList();
        saveApplicationData();
    }

    // Creates an independently editable copy with an available name.
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
            false,
            selectedProfile.getSettings().copy()
        );
        profiles.add(copy);
        selectProfile(copy);
        saveApplicationData();
    }

    // Checks names without case sensitivity, optionally excluding one profile.
    private boolean profileNameExists(String name, Profile excluded) {
        return profiles.stream().anyMatch(profile ->
                profile != excluded && profile.getName().equalsIgnoreCase(name));
    }

    // Makes the selected profile the only default profile.
    @FXML
    private void handleSetDefault() {
        if (selectedProfile == null) return;
        profiles.forEach(profile -> profile.setDefault(false));
        selectedProfile.setDefault(true);
        selectProfile(selectedProfile);
        saveApplicationData();
    }

    // Deletes a non-default profile and selects the default or first profile.
    @FXML
    private void handleDeleteProfile() {
        if (selectedProfile == null || selectedProfile.isDefault()) return;
        profiles.remove(selectedProfile);
        selectProfile(profiles.stream()
                .filter(Profile::isDefault)
                .findFirst()
                .orElse(profiles.get(0)));
        saveApplicationData();
    }

    // Opens the clicker page to edit the selected configuration.
    @FXML private void handleEditConfiguration() { showPage(clickerPage); }
    // Displays the existing warning dialog with a title and message.
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }


    /* SETTINGS */
    // Restores settings and registers their existing change handlers.
    private void initializeSettings() {
        initializeGeneralSettings();
        initializeHotkeyFields();
        initializeAccentSettings();
        initializeStartupSetting();
        initializeTraySetting();
        initializeAlwaysOnTopSetting();
    }

    // Restores the existing general options and UI-scale listener.
    private void initializeGeneralSettings() {
        setupCombo(languageCombo, "English", "English");
        setupCombo(uiScaleCombo, savedUiScale, "75%", "100% (Default)", "Fill Window");

        if (!"Fill Window".equals(savedUiScale))
            lastNormalUiScale = savedUiScale;

        uiScaleCombo.valueProperty().addListener((obs, oldValue, value) -> {
            if (value == null) return;

            if (!"Fill Window".equals(value))
                lastNormalUiScale = value;

            applySelectedUiScale(value);

            windowSnapped = false;
            savedUiScale = value;
            saveApplicationData();
        });

        setupCombo(updateCombo, savedUpdatePreference, "Automatically", "Manually", "Never");
        updateCombo.valueProperty().addListener((obs, oldValue, value) -> {
            if (value == null) return;
            savedUpdatePreference = value;
            saveApplicationData();
        });
        versionLabel.setText("Version " + UpdateService.currentVersion());
    }

    // Called after the window is visible so update dialogs have an owner.
    public void initializeUpdates(Consumer<String> openWebPage) {
        this.openWebPage = openWebPage;
        if ("Automatically".equals(savedUpdatePreference))
            checkForUpdates(false);
    }

    @FXML
    private void handleCheckForUpdates() {
        checkForUpdates(true);
    }

    private void checkForUpdates(boolean manual) {
        if (checkingUpdates) return;
        checkingUpdates = true;
        // Keep disabling the button from moving focus to the next control.
        if (manual) appRoot.requestFocus();
        checkUpdatesButton.setDisable(true);
        checkUpdatesButton.setText("Checking...");
        Task<UpdateService.Result> task = new Task<>() {
            @Override
            protected UpdateService.Result call() throws Exception {
                return updateService.check(UpdateService.currentVersion());
            }
        };
        task.setOnSucceeded(event -> {
            finishUpdateCheck();
            // Respect a preference changed while the startup check was running.
            if (!manual && !"Automatically".equals(savedUpdatePreference)) return;
            UpdateService.Result result = task.getValue();
            if (result.status() == UpdateService.Status.AVAILABLE) {
                ButtonType download = new ButtonType("Open Download Page");
                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        "CustomClicker " + result.version() + " is available. You have "
                                + UpdateService.currentVersion() + ".", download, ButtonType.CANCEL);
                alert.setTitle("Update available");
                if (showUpdateDialog(alert) == download && openWebPage != null)
                    openWebPage.accept(UpdateService.RELEASES_URL);
            } else if (manual) {
                String message = result.status() == UpdateService.Status.NO_RELEASE
                        ? "No published release is available yet."
                        : "You're up to date. You have CustomClicker " + UpdateService.currentVersion() + ".";
                Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
                alert.setTitle("Check for updates");
                showUpdateDialog(alert);
            }
        });
        task.setOnFailed(event -> {
            finishUpdateCheck();
            if (manual) {
                Alert alert = new Alert(Alert.AlertType.WARNING,
                        "Unable to check for updates. Check your internet connection "
                                + "and try again.\n\n" + task.getException().getMessage(), ButtonType.OK);
                alert.setTitle("Update check failed");
                showUpdateDialog(alert);
            }
        });
        Thread worker = new Thread(task, "update-check");
        worker.setDaemon(true);
        worker.start();
    }

    private void finishUpdateCheck() {
        checkingUpdates = false;
        checkUpdatesButton.setDisable(false);
        checkUpdatesButton.setText("Check for Updates Now");
    }

    private ButtonType showUpdateDialog(Alert alert) {
        alert.setHeaderText(null);
        alert.initOwner(appRoot.getScene().getWindow());
        try {
            return alert.showAndWait().orElse(ButtonType.CANCEL);
        } finally {
            // Return focus to the background, leaving the settings controls unselected.
            appRoot.requestFocus();
        }
    }

    // Displays restored hotkeys and connects their capture actions.
    private void initializeHotkeyFields() {
        startHotkeyField.setText(NativeKeyEvent.getKeyText(startHotkeyCode));
        startHotkeyField.setEditable(false);
        nextProfileHotkeyField.setText(NativeKeyEvent.getKeyText(nextProfileHotkeyCode));
        nextProfileHotkeyField.setEditable(false);

        startHotkeyField.setOnMouseClicked(event -> beginStartHotkeyCapture());
        nextProfileHotkeyField.setOnMouseClicked(event -> beginNextProfileHotkeyCapture());
    }

    // Connects the existing accent toggles and restores the saved accent.
    private void initializeAccentSettings() {
        setupAccentButton(greenAccent, "green");
        setupAccentButton(blueAccent, "blue");
        setupAccentButton(purpleAccent, "purple");
        setupAccentButton(pinkAccent, "pink");
        setupAccentButton(orangeAccent, "orange");
        setupAccentButton(yellowAccent, "yellow");
        setupAccentButton(cyanAccent, "cyan");

        selectAccent(savedThemeAccent);
    }

    // Connects the startup toggle while preserving its rollback on registration failure.
    private void initializeStartupSetting() {
        launchStartupCheck.setSelected(savedLaunchAtStartup);
        launchStartupCheck.setOnAction(event -> {
            boolean enabled = launchStartupCheck.isSelected();

            try {
                startupService.setLaunchAtStartup(enabled);
                savedLaunchAtStartup = enabled;
                saveApplicationData();
            } catch (Exception e) {
                launchStartupCheck.setSelected(savedLaunchAtStartup);
                showAlert("Startup error", e.getMessage());
            }
        });
    }

    // Connects the tray toggle and retains its existing installation-failure behavior.
    private void initializeTraySetting() {
        minimizeToTrayCheck.setSelected(savedMinimizeToTray);
        minimizeToTrayCheck.setOnAction(event -> {
            boolean enabled = minimizeToTrayCheck.isSelected();
            if (enabled && !installTray()) {
                minimizeToTrayCheck.setSelected(false);
                return;
            }
            if (!enabled)
                trayService.remove();

            savedMinimizeToTray = enabled;
            saveApplicationData();
        });
    }

    // Restores the always-on-top toggle before registering its change listener.
    private void initializeAlwaysOnTopSetting() {
        alwaysOnTopCheck.setSelected(savedAlwaysOnTop);
        alwaysOnTopCheck.selectedProperty().addListener((obs, oldValue, enabled) -> {
            Stage stage = (Stage) alwaysOnTopCheck.getScene().getWindow();

            if (stage != null)
                stage.setAlwaysOnTop(enabled);

            savedAlwaysOnTop = enabled;
            saveApplicationData();
        });
    }

    // Applies and persists an accent when its existing toggle is selected.
    private void setupAccentButton(ToggleButton button, String accent) {
        setupTab(button, () -> {
            selectAccent(accent);
            saveApplicationData();
        });
    }

    // Synchronizes accent toggles and applies the selected color.
    private void selectAccent(String accent) {
        savedThemeAccent = accent;
        greenAccent.setSelected("green".equals(accent));
        blueAccent.setSelected("blue".equals(accent));
        purpleAccent.setSelected("purple".equals(accent));
        pinkAccent.setSelected("pink".equals(accent));
        orangeAccent.setSelected("orange".equals(accent));
        yellowAccent.setSelected("yellow".equals(accent));
        cyanAccent.setSelected("cyan".equals(accent));
        applyAccent(accent);
    }

    // Applies the saved scale after the application window is shown.
    public void applyInitialUiScale() {
        applySelectedUiScale(uiScaleCombo.getValue());
    }

    // Maps the existing scale options to their window dimensions.
    private void applySelectedUiScale(String value) {
        if ("75%".equals(value))
            applyUiScale(0.75, false);
        else if ("Fill Window".equals(value))
            applyUiScale(1.0, true);
        else
            applyUiScale(1.0, false);
    }

    // Applies the saved always-on-top setting to the window.
    public void applyInitialAlwaysOnTop() {
        Stage stage = (Stage) alwaysOnTopCheck.getScene().getWindow();
        stage.setAlwaysOnTop(savedAlwaysOnTop);
    }


    // Installs tray actions that dispatch back to the JavaFX thread.
    private boolean installTray() {
        try {
            trayService.install(
                    () -> Platform.runLater(this::restoreFromTray),
                    () -> Platform.runLater(this::exitApplication)
            );
            return true;
        } catch (AWTException e) {
            showAlert("System tray error", e.getMessage());
            return false;
        }
    }

    // Shows and focuses the application window.
    private void restoreFromTray() {
        Stage stage = (Stage) appRoot.getScene().getWindow();
        stage.show();
        stage.setIconified(false);
        stage.toFront();
        stage.requestFocus();
    }

    // Stops background services and removes the tray icon before exiting.
    private void exitApplication() {
        if (autoClickService != null)
            autoClickService.stop();

        if (globalHotkeyService != null)
            globalHotkeyService.stop();

        trayService.remove();
        Platform.exit();
    }

    // Applies the existing content transform and window dimensions.
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

    // Checks whether a mouse event targets a title-bar button or its children.
    private boolean isWindowButton(MouseEvent event) {
        if (!(event.getTarget() instanceof Node node))
            return false;

        while (node != null) {
            if (node instanceof Button)
                return true;

            node = node.getParent();
        }

        return false;
    }

    // Applies the existing color associated with an accent name.
    private void applyAccent(String accent) {
        String color = switch (accent) {
            case "blue" -> "#3688f5";
            case "purple" -> "#a14de0";
            case "pink" -> "#f04385";
            case "orange" -> "#ff863a";
            case "yellow" -> "#ffcd36";
            case "cyan" -> "#00d9e9";
            default -> "#16e889";
        };
        appRoot.setStyle("-fx-accent: " + color + ";");
    }

    // Captures and validates a replacement start/stop hotkey.
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
            if (!GlobalHotkeyService.isFunctionKey(keyCode)) {
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
            saveApplicationData();
        }));
    }

    // Captures and validates a replacement next-profile hotkey.
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
            if (!GlobalHotkeyService.isFunctionKey(keyCode)) {
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
            saveApplicationData();
        }));
    }

    /* APPLICATION CONTROLS */
    // Stops the active run or validates and starts an independent click configuration.
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

    // Updates the start button and status indicators for the engine state.
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

    // Starts the existing repeating animation on the status dot.
    private void startStatusPulse() {
        stopStatusPulse();
        statusPulse = new FadeTransition(Duration.millis(700), statusDot);
        statusPulse.setFromValue(1.0);
        statusPulse.setToValue(0.3);
        statusPulse.setCycleCount(FadeTransition.INDEFINITE);
        statusPulse.setAutoReverse(true);
        statusPulse.play();
    }

    // Stops the status animation and restores full opacity.
    private void stopStatusPulse() {
        if (statusPulse != null) {
            statusPulse.stop();
            statusPulse = null;
        }
        statusDot.setOpacity(1.0);
    }

    // Minimizes the application window.
    @FXML
    private void handleMinimize(ActionEvent event) {
        getStage(event).setIconified(true);
    }

    // Toggles between the saved normal scale and Fill Window.
    @FXML
    private void handleMaximize(ActionEvent event) {
        if ("Fill Window".equals(uiScaleCombo.getValue())) {
            uiScaleCombo.setValue(lastNormalUiScale);
            windowSnapped = false;
        } else {
            normalScale = uiScale;
            normalFullscreen = false;

            uiScaleCombo.setValue("Fill Window");
            windowSnapped = true;
        }
    }

    // Hides to the tray when enabled, or exits the application.
    public void handleWindowCloseRequest() {
        if (savedMinimizeToTray && trayService.isInstalled()) {
            Stage stage = (Stage) appRoot.getScene().getWindow();
            stage.hide();
        } else {
            exitApplication();
        }
    }

    // Routes the title-bar close action through the normal close behavior.
    @FXML
    private void handleClose(ActionEvent event) {
        handleWindowCloseRequest();
    }

    // Restores the saved tray setting and handles installation failure.
    public void initializeTray() {
        if (savedMinimizeToTray && !installTray()) {
            savedMinimizeToTray = false;
            minimizeToTrayCheck.setSelected(false);
            saveApplicationData();
        }
    }

    // Records drag offsets and restores a snapped window before dragging.
    @FXML
    private void handleWindowPressed(MouseEvent event) {
        if (isWindowButton(event)) return;
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

    // Moves the window and applies the existing top-edge snap behavior.
    @FXML
    private void handleWindowDragged(MouseEvent event) {
        if (isWindowButton(event)) return;
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

    // Returns the window that owns an action event's source.
    private Stage getStage(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }
}
