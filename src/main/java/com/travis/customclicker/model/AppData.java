package com.travis.customclicker.model;

import java.util.ArrayList;
import java.util.List;

public class AppData {

    private List<Profile> profiles = new ArrayList<>();
    private String selectedProfileName;
    private Integer startHotkeyCode;
    private Integer nextProfileHotkeyCode;

    public AppData() {
    }

    public AppData(List<Profile> profiles, String selectedProfileName, Integer startHotkeyCode, Integer nextProfileHotkeyCode) {
        this.profiles = profiles;
        this.selectedProfileName = selectedProfileName;
        this.startHotkeyCode = startHotkeyCode;
        this.nextProfileHotkeyCode = nextProfileHotkeyCode;
    }

    public List<Profile> getProfiles() { return profiles; }
    public void setProfiles(List<Profile> profiles) { this.profiles = profiles; }

    public String getSelectedProfileName() { return selectedProfileName; }
    public void setSelectedProfileName(String selectedProfileName) { this.selectedProfileName = selectedProfileName; }

    public Integer getStartHotkeyCode() { return startHotkeyCode; }
    public void setStartHotkeyCode(Integer startHotkeyCode) { this.startHotkeyCode = startHotkeyCode; }

    public Integer getNextProfileHotkeyCode() { return nextProfileHotkeyCode; }
    public void setNextProfileHotkeyCode(Integer nextProfileHotkeyCode) { this.nextProfileHotkeyCode = nextProfileHotkeyCode; }
}