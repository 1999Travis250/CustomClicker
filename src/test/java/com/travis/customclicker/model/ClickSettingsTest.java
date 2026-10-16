package com.travis.customclicker.model;

import com.google.gson.Gson;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class ClickSettingsTest {

    // Checks every field in a duplicated profile and verifies edits stay independent.
    @Test
    public void copyPreservesAllSettingsWithoutSharingTheInstance() {
        ClickSettings original = new ClickSettings();
        original.setTimingMode(ClickSettings.TimingMode.RANDOM_INTERVAL);
        original.setMouseButton(ClickSettings.MouseButton.MIDDLE);
        original.setClickType(ClickSettings.ClickType.DOUBLE);
        original.setRepeatMode(ClickSettings.RepeatMode.FIXED_AMOUNT);
        original.setTargetMode(ClickSettings.TargetMode.FIXED_POSITION);
        original.setMinCps(2.5);
        original.setMaxCps(9.5);
        original.setFixedValue(75);
        original.setMinInterval(25);
        original.setMaxInterval(125);
        original.setX(-800);
        original.setY(600);
        original.setRepeatAmount(30);

        ClickSettings copy = original.copy();

        assertNotSame(original, copy);
        Gson gson = new Gson();
        assertEquals(gson.toJsonTree(original), gson.toJsonTree(copy));

        copy.setX(100);
        copy.setRepeatAmount(1);
        assertEquals(-800, original.getX());
        assertEquals(30, original.getRepeatAmount());
    }
}
