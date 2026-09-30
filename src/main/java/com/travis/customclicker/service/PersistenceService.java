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

    public PersistenceService() {
        dataFile = getDataDirectory().resolve("customclicker-data.json");
    }

    public void saveData(AppData data) throws IOException {
        Files.createDirectories(dataFile.getParent());
        Files.writeString(dataFile, gson.toJson(data));
    }

    public AppData loadData() throws IOException {
        if (!Files.exists(dataFile)) return new AppData();

        String json = Files.readString(dataFile);
        AppData data = gson.fromJson(json, AppData.class);
        return data != null ? data : new AppData();
    }

    public Path getDataFile() {
        return dataFile;
    }

    private Path getDataDirectory() {
        String appData = System.getenv("APPDATA");

        if (appData != null && !appData.isBlank())
            return Path.of(appData, "CustomClicker");

        return Path.of(System.getProperty("user.home"), ".customclicker");
    }
}