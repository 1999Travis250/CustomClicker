package com.travis.customclicker.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.travis.customclicker.model.AppData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PersistenceService {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path dataFile;

    // Uses the existing per-user application data location.
    public PersistenceService() {
        this(getDataDirectory().resolve("customclicker-data.json"));
    }

    // Allows persistence tests to use an isolated file instead of user data.
    PersistenceService(Path dataFile) {
        this.dataFile = dataFile;
    }

    // Writes all profiles and application settings using the existing JSON schema.
    public void saveData(AppData data) throws IOException {
        Files.createDirectories(dataFile.getParent());
        Files.writeString(dataFile, gson.toJson(data));
    }

    // Loads saved data, or returns defaults when no data has been saved.
    public AppData loadData() throws IOException {
        if (!Files.exists(dataFile)) return new AppData();

        String json = Files.readString(dataFile);
        AppData data = gson.fromJson(json, AppData.class);
        return data != null ? data : new AppData();
    }

    // Returns the location used for both loading and saving.
    public Path getDataFile() {
        return dataFile;
    }

    // Resolves the Windows data directory with the existing home-directory fallback.
    private static Path getDataDirectory() {
        String appData = System.getenv("APPDATA");

        if (appData != null && !appData.isBlank())
            return Path.of(appData, "CustomClicker");

        return Path.of(System.getProperty("user.home"), ".customclicker");
    }
}
