package com.travis.customclicker.service;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.travis.customclicker.model.AppData;
import com.travis.customclicker.model.ClickSettings;
import com.travis.customclicker.model.Profile;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class PersistenceServiceTest {

    private static final String SAVED_DATA = """
            {
              "profiles": [{
                "name": "Existing profile",
                "description": "Saved settings — preserved",
                "isDefault": true,
                "settings": {
                  "timingMode": "RANDOM_CPS",
                  "mouseButton": "RIGHT",
                  "clickType": "DOUBLE",
                  "repeatMode": "FIXED_AMOUNT",
                  "targetMode": "FIXED_POSITION",
                  "minCps": 2.5,
                  "maxCps": 7.5,
                  "fixedValue": 42,
                  "minInterval": 80,
                  "maxInterval": 250,
                  "x": -1200,
                  "y": 400,
                  "repeatAmount": 23
                }
              }],
              "selectedProfileName": "Existing profile",
              "startHotkeyCode": 64,
              "nextProfileHotkeyCode": 65,
              "themeAccent": "purple",
              "uiScale": "75%",
              "alwaysOnTop": true,
              "launchAtStartup": true,
              "minimizeToTray": true
            }
            """;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    // Confirms that saving and reloading retain every existing JSON field and value.
    @Test
    public void existingJsonRoundTripsWithoutSchemaChanges() throws Exception {
        Path file = temporaryFolder.getRoot().toPath().resolve("data.json");
        Files.writeString(file, SAVED_DATA);
        PersistenceService service = new PersistenceService(file);

        AppData loaded = service.loadData();
        Profile profile = loaded.getProfiles().get(0);
        assertEquals("Existing profile", profile.getName());
        assertTrue(profile.isDefault());
        assertEquals(ClickSettings.TimingMode.RANDOM_CPS, profile.getSettings().getTimingMode());
        assertEquals(2.5, profile.getSettings().getMinCps(), 0.0);
        assertEquals(-1200, profile.getSettings().getX());

        service.saveData(loaded);

        assertEquals(JsonParser.parseString(SAVED_DATA), JsonParser.parseString(Files.readString(file)));
        assertEquals("Existing profile", service.loadData().getSelectedProfileName());
    }

    // Confirms older files keep the defaults for fields added in later versions.
    @Test
    public void olderJsonRetainsDefaultsForMissingSettings() throws Exception {
        Path file = temporaryFolder.getRoot().toPath().resolve("legacy.json");
        Files.writeString(file, """
                {"profiles":[{"name":"Legacy","description":"","isDefault":true,"settings":{}}]}
                """);

        AppData loaded = new PersistenceService(file).loadData();
        ClickSettings settings = loaded.getProfiles().get(0).getSettings();

        assertNull(loaded.getStartHotkeyCode());
        assertNull(loaded.getUiScale());
        assertFalse(loaded.isAlwaysOnTop());
        assertFalse(loaded.isLaunchAtStartup());
        assertFalse(loaded.isMinimizeToTray());
        assertEquals(ClickSettings.TimingMode.FIXED_INTERVAL, settings.getTimingMode());
        assertEquals(ClickSettings.MouseButton.LEFT, settings.getMouseButton());
        assertEquals(100, settings.getFixedValue());
        assertEquals(0.5, settings.getMinCps(), 0.0);
        assertEquals(1.5, settings.getMaxCps(), 0.0);
        assertEquals(10, settings.getRepeatAmount());
    }

    // Confirms the first save creates the application data directory as needed.
    @Test
    public void missingFileLoadsDefaultsAndSaveCreatesParentDirectory() throws Exception {
        Path file = temporaryFolder.getRoot().toPath().resolve("nested/data.json");
        PersistenceService service = new PersistenceService(file);

        AppData loaded = service.loadData();
        assertTrue(loaded.getProfiles().isEmpty());
        assertFalse(Files.exists(file));

        service.saveData(loaded);

        assertTrue(Files.isRegularFile(file));
        assertTrue(service.loadData().getProfiles().isEmpty());
    }

    // Preserves the current fallback for empty files and an explicit JSON null.
    @Test
    public void emptyAndNullJsonLoadDefaults() throws Exception {
        Path file = temporaryFolder.getRoot().toPath().resolve("empty.json");
        PersistenceService service = new PersistenceService(file);
        for (String json : new String[]{"", "null"}) {
            Files.writeString(file, json);
            assertTrue(service.loadData().getProfiles().isEmpty());
        }
    }

    // Leaves malformed-file recovery to the caller, matching existing behavior.
    @Test
    public void malformedJsonIsReportedWithoutChangingTheFile() throws Exception {
        Path file = temporaryFolder.getRoot().toPath().resolve("invalid.json");
        Files.writeString(file, "{invalid");

        assertThrows(JsonSyntaxException.class, () -> new PersistenceService(file).loadData());

        assertEquals("{invalid", Files.readString(file));
    }
}
