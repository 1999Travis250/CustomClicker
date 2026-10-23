package com.travis.customclicker.service;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class SingleInstanceServiceTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void secondaryLaunchesActivateTheOwnerWithoutBecomingOwners() throws Exception {
        Path directory = temporaryFolder.getRoot().toPath();
        LinkedBlockingQueue<String> activations = new LinkedBlockingQueue<>();
        try (SingleInstanceService owner = new SingleInstanceService(directory)) {
            assertTrue(owner.start(() -> activations.add("owner")));
            for (int launch = 0; launch < 3; launch++) {
                try (SingleInstanceService secondary = new SingleInstanceService(directory)) {
                    assertFalse(secondary.start(() -> activations.add("secondary")));
                    assertEquals("owner", activations.poll(3, TimeUnit.SECONDS));
                }
            }
        }
    }

    @Test
    public void exitReleasesOwnershipAndLeftoverFilesDoNotBlockRestart() throws Exception {
        Path directory = temporaryFolder.getRoot().toPath();
        try (SingleInstanceService first = new SingleInstanceService(directory)) {
            assertTrue(first.start(() -> {}));
        }
        assertTrue(Files.exists(directory.resolve("instance.lock")));
        try (SingleInstanceService restarted = new SingleInstanceService(directory)) {
            assertTrue(restarted.start(() -> {}));
        }
    }

    @Test
    public void separateUserDirectoriesCanEachOwnAnInstance() throws Exception {
        Path directory = temporaryFolder.getRoot().toPath();
        try (SingleInstanceService first = new SingleInstanceService(directory.resolve("first"));
             SingleInstanceService second = new SingleInstanceService(directory.resolve("second"))) {
            assertTrue(first.start(() -> {}));
            assertTrue(second.start(() -> {}));
        }
    }

    @Test
    public void separateProcessActivatesAndCrashReleasesTheLock() throws Exception {
        Path directory = temporaryFolder.getRoot().toPath();
        String java = Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
        String classpath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        Process process = new ProcessBuilder(java, "-cp", classpath,
                OwnerProcess.class.getName(), directory.toString()).redirectError(ProcessBuilder.Redirect.INHERIT).start();
        try {
            var output = process.inputReader();
            assertEquals("READY", CompletableFuture.supplyAsync(() -> readLine(output)).get(5, TimeUnit.SECONDS));
            try (SingleInstanceService secondary = new SingleInstanceService(directory)) {
                assertFalse(secondary.start(() -> fail("Secondary became owner")));
                assertEquals("ACTIVATED", CompletableFuture.supplyAsync(() -> readLine(output)).get(5, TimeUnit.SECONDS));
            }
        } finally {
            process.destroyForcibly();
            assertTrue(process.waitFor(5, TimeUnit.SECONDS));
        }
        try (SingleInstanceService restarted = new SingleInstanceService(directory)) {
            assertTrue(restarted.start(() -> {}));
        }
    }

    private static String readLine(java.io.BufferedReader reader) {
        try {
            return reader.readLine();
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    public static class OwnerProcess {
        public static void main(String[] args) throws Exception {
            try (SingleInstanceService owner = new SingleInstanceService(Path.of(args[0]))) {
                if (!owner.start(() -> System.out.println("ACTIVATED")))
                    throw new IllegalStateException("Could not become owner");
                System.out.println("READY");
                System.in.read();
            }
        }
    }
}
