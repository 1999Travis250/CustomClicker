
package com.travis.customclicker.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WindowsStartupService {

    private static final String REGISTRY_KEY =
            "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";

    private static final String APP_NAME = "CustomClicker";

    public void setLaunchAtStartup(boolean enabled)
            throws IOException, InterruptedException {

        if (!System.getProperty("os.name").toLowerCase().contains("win"))
            throw new IOException("Startup registration requires Windows.");

        ProcessBuilder builder;

        if (enabled) {
            String command = ProcessHandle.current()
                    .info()
                    .command()
                    .orElseThrow(() -> new IOException("Application path not found."));

            Path executable = Path.of(command).toAbsolutePath();

            if (!Files.isRegularFile(executable)
                    || !executable.getFileName().toString()
                    .equalsIgnoreCase(APP_NAME + ".exe")) {
                throw new IOException(
                        "Launch at startup requires the packaged CustomClicker.exe.");
            }

            builder = new ProcessBuilder(
                    "reg.exe", "add", REGISTRY_KEY,
                    "/v", APP_NAME,
                    "/t", "REG_SZ",
                    "/d", "\"" + executable + "\"",
                    "/f"
            );
        } else {
            builder = new ProcessBuilder(
                    "reg.exe", "delete", REGISTRY_KEY,
                    "/v", APP_NAME, "/f"
            );
        }

        Process process = builder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0)
            throw new IOException("Unable to update Windows startup.");
    }
}
