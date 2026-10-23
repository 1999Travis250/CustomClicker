package com.travis.customclicker.model;

import java.util.ArrayList;
import java.util.List;

public class AppData {

    private List<Profile> profiles = new ArrayList<>();
    private String selectedProfileName;
    private Integer startHotkeyCode;
    private Integer nextProfileHotkeyCode;
    private String themeAccent;
    private String uiScale;
    private boolean alwaysOnTop;
    private boolean launchAtStartup;
    private boolean minimizeToTray;
    private String updatePreference;

    public String getUpdatePreference() {
        return updatePreference;
    }

    public void setUpdatePreference(String updatePreference) {
        this.updatePreference = updatePreference;
    }

    // Keeps a no-argument constructor for JSON deserialization.
    public AppData() {
    }

    // Initializes the model with the supplied values.
    public AppData(List<Profile> profiles, String selectedProfileName,
                   Integer startHotkeyCode, Integer nextProfileHotkeyCode,
                   String themeAccent, String uiScale, boolean alwaysOnTop,
                   boolean launchAtStartup, boolean minimizeToTray) {
        this.profiles = profiles;
        this.selectedProfileName = selectedProfileName;
        this.startHotkeyCode = startHotkeyCode;
        this.nextProfileHotkeyCode = nextProfileHotkeyCode;
        this.themeAccent = themeAccent;
        this.uiScale = uiScale;
        this.alwaysOnTop = alwaysOnTop;
        this.launchAtStartup = launchAtStartup;
        this.minimizeToTray = minimizeToTray;
    }

    // Returns the profiles value.
    public List<Profile> getProfiles() {
        return profiles;
    }

    // Sets the profiles value.
    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
    }

    // Returns the selected profile name value.
    public String getSelectedProfileName() {
        return selectedProfileName;
    }

    // Sets the selected profile name value.
    public void setSelectedProfileName(String selectedProfileName) {
        this.selectedProfileName = selectedProfileName;
    }

    // Returns the start hotkey code value.
    public Integer getStartHotkeyCode() {
        return startHotkeyCode;
    }

    // Sets the start hotkey code value.
    public void setStartHotkeyCode(Integer startHotkeyCode) {
        this.startHotkeyCode = startHotkeyCode;
    }

    // Returns the next profile hotkey code value.
    public Integer getNextProfileHotkeyCode() {
        return nextProfileHotkeyCode;
    }

    // Sets the next profile hotkey code value.
    public void setNextProfileHotkeyCode(Integer nextProfileHotkeyCode) {
        this.nextProfileHotkeyCode = nextProfileHotkeyCode;
    }

    // Returns the theme accent value.
    public String getThemeAccent() {
        return themeAccent;
    }

    // Sets the theme accent value.
    public void setThemeAccent(String themeAccent) {
        this.themeAccent = themeAccent;
    }

    // Returns the UI scale option.
    public String getUiScale() {
        return uiScale;
    }

    // Sets the UI scale option.
    public void setUiScale(String uiScale) {
        this.uiScale = uiScale;
    }

    // Returns the always on top value.
    public boolean isAlwaysOnTop() {
        return alwaysOnTop;
    }

    // Sets the always on top value.
    public void setAlwaysOnTop(boolean alwaysOnTop) {
        this.alwaysOnTop = alwaysOnTop;
    }

    // Returns the launch at startup value.
    public boolean isLaunchAtStartup() {
        return launchAtStartup;
    }

    // Sets the launch at startup value.
    public void setLaunchAtStartup(boolean launchAtStartup) {
        this.launchAtStartup = launchAtStartup;
    }

    // Returns the minimize to tratarget Y coordinate.
    public boolean isMinimizeToTray() {
        return minimizeToTray;
    }

    // Sets the minimize to tratarget Y coordinate.
    public void setMinimizeToTray(boolean minimizeToTray) {
        this.minimizeToTray = minimizeToTray;
    }
}
