package com.travis.customclicker.service;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class GlobalHotkeyService implements NativeKeyListener {

    private int hotkeyCode = NativeKeyEvent.VC_F6;
    private boolean hotkeyPressed = false;
    private Runnable hotkeyAction;

    public void start(Runnable hotkeyAction) throws NativeHookException {
        this.hotkeyAction = hotkeyAction;

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
    }

    public int getHotkeyCode() {
        return hotkeyCode;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent event) {
        if (event.getKeyCode() == hotkeyCode && !hotkeyPressed) {
            hotkeyPressed = true;

            if (hotkeyAction != null)
                hotkeyAction.run();
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent event) {
        if (event.getKeyCode() == hotkeyCode)
            hotkeyPressed = false;
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent event) {
    }
}
