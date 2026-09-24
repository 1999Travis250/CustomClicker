package com.travis.customclicker.service;

import com.travis.customclicker.model.ClickSettings;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutoClickService {

    private final Robot robot;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private Thread clickThread;

    public AutoClickService() throws AWTException {
        robot = new Robot();
        robot.setAutoDelay(0);
    }

    public boolean isRunning() {
        return running.get();
    }

    public void start(ClickSettings settings, Runnable onStopped) {
        if (running.get()) return;

        running.set(true);

        clickThread = new Thread(() -> {
            try {
                runClickLoop(settings);
            } finally {
                running.set(false);

                if (onStopped != null)
                    onStopped.run();
            }
        });

        clickThread.setDaemon(true);
        clickThread.setName("CustomClicker-ClickEngine");
        clickThread.start();
    }

    public void stop() {
        running.set(false);

        if (clickThread != null)
            clickThread.interrupt();
    }

    private void runClickLoop(ClickSettings settings) {
        long completedClicks = 0;

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            return;
        }

        while (running.get()) {

            if (settings.getRepeatMode() == ClickSettings.RepeatMode.FIXED_AMOUNT &&
                    completedClicks >= settings.getRepeatAmount()) {
                break;
            }

            long cycleStart = System.nanoTime();

            performClick(settings);
            completedClicks++;

            if (!running.get()) break;

            long targetDelayNanos = calculateDelay(settings) * 1_000_000L;
            long elapsedNanos = System.nanoTime() - cycleStart;
            long remainingNanos = targetDelayNanos - elapsedNanos;

            if (remainingNanos > 0) {
                try {
                    long millis = remainingNanos / 1_000_000L;
                    int nanos = (int) (remainingNanos % 1_000_000L);

                    Thread.sleep(millis, nanos);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    private void performClick(ClickSettings settings) {
        if (settings.getTargetMode() == ClickSettings.TargetMode.FIXED_POSITION)
            robot.mouseMove(settings.getX(), settings.getY());

        int buttonMask = getButtonMask(settings.getMouseButton());

        click(buttonMask);

        if (settings.getClickType() == ClickSettings.ClickType.DOUBLE) {
            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            click(buttonMask);
        }
    }

    private void click(int buttonMask) {
        robot.mousePress(buttonMask);
        robot.mouseRelease(buttonMask);
    }

    private int getButtonMask(ClickSettings.MouseButton button) {
        return switch (button) {
            case RIGHT -> InputEvent.BUTTON3_DOWN_MASK;
            case MIDDLE -> InputEvent.BUTTON2_DOWN_MASK;
            default -> InputEvent.BUTTON1_DOWN_MASK;
        };
    }

    private long calculateDelay(ClickSettings settings) {
        return switch (settings.getTimingMode()) {

            case FIXED_INTERVAL ->
                    Math.max(1, settings.getFixedValue());

            case FIXED_CPS ->
                    cpsToDelay(settings.getFixedValue());

            case RANDOM_CPS -> {
                double cps = ThreadLocalRandom.current().nextDouble(
                        settings.getMinCps(),
                        settings.getMaxCps() + 1
                );

                yield cpsToDelay(cps);
            }

            case RANDOM_INTERVAL ->
                    ThreadLocalRandom.current().nextLong(
                            settings.getMinInterval(),
                            (long) settings.getMaxInterval() + 1
                    );
        };
    }

    private long cpsToDelay(double cps) {
        if (cps <= 0) return 1000;

        return Math.max(1, Math.round(1000.0 / cps));
    }
}