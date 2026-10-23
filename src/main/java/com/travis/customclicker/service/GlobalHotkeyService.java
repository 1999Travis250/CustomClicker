package com.travis.customclicker.service;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.util.function.Consumer;

public class GlobalHotkeyService implements NativeKeyListener {

    private int hotkeyCode = NativeKeyEvent.VC_F6;
    private int nextProfileHotkeyCode = NativeKeyEvent.VC_F7;

    private boolean hotkeyPressed = false;
    private boolean nextProfileHotkeyPressed = false;

    private Runnable hotkeyAction;
    private Runnable nextProfileAction;

    private boolean capturingKey = false;
    private Consumer<Integer> captureAction;

    // Registers the native listener and the two hotkey actions.
    public void start(Runnable hotkeyAction, Runnable nextProfileAction) throws NativeHookException {
        this.hotkeyAction = hotkeyAction;
        this.nextProfileAction = nextProfileAction;

        if (!GlobalScreen.isNativeHookRegistered())
            GlobalScreen.registerNativeHook();

        GlobalScreen.addNativeKeyListener(this);
    }

    // Removes the listener and unregisters the native hook.
    public void stop() {
        GlobalScreen.removeNativeKeyListener(this);

        try {
            if (GlobalScreen.isNativeHookRegistered())
                GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    // Updates the start/stop assignment and clears its held-key state.
    public void setHotkeyCode(int hotkeyCode) {
        if (!isFunctionKey(hotkeyCode))
            throw new IllegalArgumentException("Hotkeys must be F1-F12.");
        this.hotkeyCode = hotkeyCode;
        hotkeyPressed = false;
    }

    // Returns the start/stop assignment.
    public int getHotkeyCode() {
        return hotkeyCode;
    }

    // Updates the profile-cycle assignment and clears its held-key state.
    public void setNextProfileHotkeyCode(int nextProfileHotkeyCode) {
        if (!isFunctionKey(nextProfileHotkeyCode))
            throw new IllegalArgumentException("Hotkeys must be F1-F12.");
        this.nextProfileHotkeyCode = nextProfileHotkeyCode;
        nextProfileHotkeyPressed = false;
    }

    // Returns the profile-cycle assignment.
    public int getNextProfileHotkeyCode() {
        return nextProfileHotkeyCode;
    }

    // Native function-key codes are not contiguous; the gaps include other keys.
    public static boolean isFunctionKey(int keyCode) {
        return switch (keyCode) {
            case NativeKeyEvent.VC_F1, NativeKeyEvent.VC_F2, NativeKeyEvent.VC_F3,
                 NativeKeyEvent.VC_F4, NativeKeyEvent.VC_F5, NativeKeyEvent.VC_F6,
                 NativeKeyEvent.VC_F7, NativeKeyEvent.VC_F8, NativeKeyEvent.VC_F9,
                 NativeKeyEvent.VC_F10, NativeKeyEvent.VC_F11, NativeKeyEvent.VC_F12 -> true;
            default -> false;
        };
    }

    // Captures the next key press instead of dispatching a hotkey action.
    public void captureNextKey(Consumer<Integer> captureAction) {
        this.captureAction = captureAction;
        capturingKey = true;

        hotkeyPressed = false;
        nextProfileHotkeyPressed = false;
    }

    // Clears the pending key-capture callback.
    public void cancelKeyCapture() {
        capturingKey = false;
        captureAction = null;
    }

    // Reports whether the next key press will be captured.
    public boolean isCapturingKey() {
        return capturingKey;
    }

    // Dispatches capture or hotkey actions while suppressing held-key repeats.
    @Override
    public void nativeKeyPressed(NativeKeyEvent event) {
        if (capturingKey) {
            capturingKey = false;

            Consumer<Integer> action = captureAction;
            captureAction = null;

            if (action != null)
                action.accept(event.getKeyCode());

            return;
        }

        if (event.getKeyCode() == hotkeyCode && !hotkeyPressed) {
            hotkeyPressed = true;

            if (hotkeyAction != null)
                hotkeyAction.run();

            return;
        }

        if (event.getKeyCode() == nextProfileHotkeyCode && !nextProfileHotkeyPressed) {
            nextProfileHotkeyPressed = true;

            if (nextProfileAction != null)
                nextProfileAction.run();
        }
    }

    // Clears held-key state for the released hotkey.
    @Override
    public void nativeKeyReleased(NativeKeyEvent event) {
        if (event.getKeyCode() == hotkeyCode)
            hotkeyPressed = false;

        if (event.getKeyCode() == nextProfileHotkeyCode)
            nextProfileHotkeyPressed = false;
    }

    // Ignores typed events because hotkeys use press and release events.
    @Override
    public void nativeKeyTyped(NativeKeyEvent event) {
    }
}
