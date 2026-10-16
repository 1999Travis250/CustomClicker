package com.travis.customclicker.model;

public class Profile {

    private String name;
    private String description;
    private boolean isDefault;
    private ClickSettings settings;

    // Keeps a no-argument constructor for JSON deserialization.
    public Profile() {
    }

    // Initializes the model with the supplied values.
    public Profile(String name, String description, boolean isDefault, ClickSettings settings) {
        this.name = name;
        this.description = description;
        this.isDefault = isDefault;
        this.settings = settings;
    }

    // Returns the name value.
    public String getName() {
        return name;
    }


    // Sets the name value.
    public void setName(String name) {
        this.name = name;
    }

    // Returns the description value.
    public String getDescription() {
        return description;
    }


    // Sets the description value.
    public void setDescription(String description) {
        this.description = description;
    }

    // Returns the default value.
    public boolean isDefault() {
        return isDefault;
    }


    // Sets the default value.
    public void setDefault(boolean defaultProfile) {
        isDefault = defaultProfile;
    }

    // Returns the settings value.
    public ClickSettings getSettings() {
        return settings;
    }


    // Sets the settings value.
    public void setSettings(ClickSettings settings) {
        this.settings = settings;
    }
}
