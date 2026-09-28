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

    public void start(Runnable hotkeyAction, Runnable nextProfileAction) throws NativeHookException {
        this.hotkeyAction = hotkeyAction;
        this.nextProfileAction = nextProfileAction;

        if (!GlobalScreen.isNativeHookRegistered())
            GlobalScreen.registerNativeHook();

        GlobalScreen.addNativeKeyListener(this);
    }

    public void stop() {
        GlobalScreen.removeNativeKeyListener(this);

        try {
            if (GlobalScreen.isNativeHookRegistered())
                GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    public void setHotkeyCode(int hotkeyCode) {
        this.hotkeyCode = hotkeyCode;
        hotkeyPressed = false;
    }

    public int getHotkeyCode() {
        return hotkeyCode;
    }

    public void setNextProfileHotkeyCode(int nextProfileHotkeyCode) {
        this.nextProfileHotkeyCode = nextProfileHotkeyCode;
        nextProfileHotkeyPressed = false;
    }

    public int getNextProfileHotkeyCode() {
        return nextProfileHotkeyCode;
    }

    public void captureNextKey(Consumer<Integer> captureAction) {
        this.captureAction = captureAction;
        capturingKey = true;

        hotkeyPressed = false;
        nextProfileHotkeyPressed = false;
    }

    public void cancelKeyCapture() {
        capturingKey = false;
        captureAction = null;
    }

    public boolean isCapturingKey() {
        return capturingKey;
    }

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

    @Override
    public void nativeKeyReleased(NativeKeyEvent event) {
        if (event.getKeyCode() == hotkeyCode)
            hotkeyPressed = false;

        if (event.getKeyCode() == nextProfileHotkeyCode)
            nextProfileHotkeyPressed = false;
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent event) {
    }
}