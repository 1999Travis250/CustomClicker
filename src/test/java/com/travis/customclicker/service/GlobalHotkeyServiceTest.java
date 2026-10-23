package com.travis.customclicker.service;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;

public class GlobalHotkeyServiceTest {

    @Test
    public void onlyF1ThroughF12CanBeAssignedToEitherHotkey() {
        GlobalHotkeyService service = new GlobalHotkeyService();
        List<Integer> allowed = List.of(
                NativeKeyEvent.VC_F1, NativeKeyEvent.VC_F2, NativeKeyEvent.VC_F3,
                NativeKeyEvent.VC_F4, NativeKeyEvent.VC_F5, NativeKeyEvent.VC_F6,
                NativeKeyEvent.VC_F7, NativeKeyEvent.VC_F8, NativeKeyEvent.VC_F9,
                NativeKeyEvent.VC_F10, NativeKeyEvent.VC_F11, NativeKeyEvent.VC_F12);

        for (int keyCode : allowed) {
            assertTrue(GlobalHotkeyService.isFunctionKey(keyCode));
            service.setHotkeyCode(keyCode);
            service.setNextProfileHotkeyCode(keyCode);
            assertEquals(keyCode, service.getHotkeyCode());
            assertEquals(keyCode, service.getNextProfileHotkeyCode());
        }

        // Includes the non-function keys between F10 and F11, plus F13-F24.
        for (int keyCode = 0; keyCode <= 0xFFFF; keyCode++) {
            if (allowed.contains(keyCode)) continue;
            int rejectedCode = keyCode;
            assertFalse("Unexpected allowed key: " + keyCode,
                    GlobalHotkeyService.isFunctionKey(keyCode));
            assertThrows(IllegalArgumentException.class,
                    () -> service.setHotkeyCode(rejectedCode));
            assertThrows(IllegalArgumentException.class,
                    () -> service.setNextProfileHotkeyCode(rejectedCode));
            assertEquals(NativeKeyEvent.VC_F12, service.getHotkeyCode());
            assertEquals(NativeKeyEvent.VC_F12, service.getNextProfileHotkeyCode());
        }
    }

    // Exercises capture directly without registering a system-wide native hook.
    @Test
    public void captureConsumesOnlyTheNextKeyPress() {
        GlobalHotkeyService service = new GlobalHotkeyService();
        List<Integer> captured = new ArrayList<>();
        service.captureNextKey(captured::add);
        assertTrue(service.isCapturingKey());

        service.nativeKeyPressed(keyPress(NativeKeyEvent.VC_F6));
        service.nativeKeyPressed(keyPress(NativeKeyEvent.VC_F7));

        assertEquals(List.of(NativeKeyEvent.VC_F6), captured);
        assertFalse(service.isCapturingKey());
        assertEquals(NativeKeyEvent.VC_F6, service.getHotkeyCode());
        assertEquals(NativeKeyEvent.VC_F7, service.getNextProfileHotkeyCode());
    }

    // Confirms cancellation prevents delivery to the discarded callback.
    @Test
    public void cancelledCaptureDoesNotReceiveTheNextKey() {
        GlobalHotkeyService service = new GlobalHotkeyService();
        List<Integer> captured = new ArrayList<>();
        service.captureNextKey(captured::add);
        service.cancelKeyCapture();

        service.nativeKeyPressed(keyPress(NativeKeyEvent.VC_F8));

        assertTrue(captured.isEmpty());
        assertFalse(service.isCapturingKey());
    }

    // Confirms a new capture request replaces the pending callback.
    @Test
    public void latestCaptureRequestReceivesTheKey() {
        GlobalHotkeyService service = new GlobalHotkeyService();
        List<Integer> first = new ArrayList<>();
        List<Integer> second = new ArrayList<>();
        service.captureNextKey(first::add);
        service.captureNextKey(second::add);

        service.nativeKeyPressed(keyPress(NativeKeyEvent.VC_ESCAPE));

        assertTrue(first.isEmpty());
        assertEquals(List.of(NativeKeyEvent.VC_ESCAPE), second);
        assertFalse(service.isCapturingKey());
    }

    // Preserves delivery of non-function keys so the controller can validate them.
    @Test
    public void capturePassesUnrestrictedKeyCodesToTheController() {
        GlobalHotkeyService service = new GlobalHotkeyService();
        List<Integer> captured = new ArrayList<>();
        service.captureNextKey(captured::add);

        service.nativeKeyPressed(keyPress(NativeKeyEvent.VC_A));

        assertEquals(List.of(NativeKeyEvent.VC_A), captured);
    }

    // Builds a native event without generating real keyboard input.
    private NativeKeyEvent keyPress(int keyCode) {
        return new NativeKeyEvent(NativeKeyEvent.NATIVE_KEY_PRESSED, 0, 0,
                keyCode, NativeKeyEvent.CHAR_UNDEFINED);
    }
}
