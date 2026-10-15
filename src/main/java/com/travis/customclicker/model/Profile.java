package com.travis.customclicker.model;

public class Profile {

    private String name;
    private String description;
    private boolean isDefault;
    private ClickSettings settings;

    public Profile() {
    }

    public Profile(String name, String description, boolean isDefault, ClickSettings settings) {
        this.name = name;
        this.description = description;
        this.isDefault = isDefault;
        this.settings = settings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public ClickSettings getSettings() {
        return settings;
    }

    public void setSettings(ClickSettings settings) {
        this.settings = settings;
    }
}