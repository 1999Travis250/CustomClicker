package com.travis.customclicker.service;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Uses an OS file lock and a local request file to coordinate launches for one user. */
public final class SingleInstanceService implements AutoCloseable {
    private final Path directory;
    private FileChannel channel;
    private FileLock lock;
    private ScheduledExecutorService listener;
    private String lastRequest = "";

    public SingleInstanceService(Path directory) {
        this.directory = directory;
    }

    // A secondary launch signals the owner and returns without starting application services.
    public boolean start(Runnable onActivate) throws IOException {
        Files.createDirectories(directory);
        Path requestFile = directory.resolve("activate.request");
        channel = FileChannel.open(directory.resolve("instance.lock"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        try {
            try {
                lock = channel.tryLock();
            } catch (OverlappingFileLockException alreadyOwnedInThisJvm) {
                lock = null;
            }
            if (lock == null) {
                channel.close();
                channel = null;
                Files.writeString(requestFile, UUID.randomUUID().toString());
                return false;
            }
            listener = Executors.newSingleThreadScheduledExecutor(task -> {
                Thread thread = new Thread(task, "instance-activation");
                thread.setDaemon(true);
                return thread;
            });
            listener.scheduleWithFixedDelay(() -> {
                try {
                    if (!Files.exists(requestFile)) return;
                    String request = Files.readString(requestFile);
                    if (!request.isBlank() && !request.equals(lastRequest)) {
                        lastRequest = request;
                        onActivate.run();
                    }
                } catch (IOException e) {
                    System.err.println("Unable to read window activation request: " + e.getMessage());
                }
            }, 0, 250, TimeUnit.MILLISECONDS);
            return true;
        } catch (IOException | RuntimeException e) {
            close();
            throw e;
        }
    }

    // OS locks are also released automatically if the process crashes; leftover files are harmless.
    @Override
    public void close() throws IOException {
        if (listener != null) {
            listener.shutdownNow();
            listener = null;
        }
        try {
            if (lock != null) lock.release();
        } finally {
            lock = null;
            if (channel != null) {
                channel.close();
                channel = null;
            }
        }
    }
}
