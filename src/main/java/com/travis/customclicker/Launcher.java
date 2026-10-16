package com.travis.customclicker;

/** Entry point for the Windows launcher; JavaFX remains responsible for application lifecycle. */
public final class Launcher {

    private Launcher() {
    }

    // Starts the existing application from the bundled runtime.
    public static void main(String[] args) {
        App.main(args);
    }
}
