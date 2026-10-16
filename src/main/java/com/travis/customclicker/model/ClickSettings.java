package com.travis.customclicker.model;

public class ClickSettings {

    public enum TimingMode {
        FIXED_INTERVAL, FIXED_CPS, RANDOM_CPS, RANDOM_INTERVAL
    }
    public enum MouseButton {
        LEFT, RIGHT, MIDDLE
    }
    public enum ClickType {
        SINGLE, DOUBLE
    }
    public enum RepeatMode {
        UNTIL_STOPPED, FIXED_AMOUNT
    }
    public enum TargetMode {
        FOLLOW_CURSOR, FIXED_POSITION
    }

    private TimingMode timingMode = TimingMode.FIXED_INTERVAL;
    private MouseButton mouseButton = MouseButton.LEFT;
    private ClickType clickType = ClickType.SINGLE;
    private RepeatMode repeatMode = RepeatMode.UNTIL_STOPPED;
    private TargetMode targetMode = TargetMode.FOLLOW_CURSOR;

    private double minCps = 0.5;
    private double maxCps = 1.5;
    private int fixedValue = 100;
    private int minInterval = 100;
    private int maxInterval = 300;
    private int x;
    private int y;
    private int repeatAmount = 10;

    // Returns the repeat amount value.
    public int getRepeatAmount() {
        return repeatAmount;
    }

    // Sets the repeat amount value.
    public void setRepeatAmount(int repeatAmount) {
        this.repeatAmount = repeatAmount;
    }

    // Returns the timing mode value.
    public TimingMode getTimingMode() {
        return timingMode;
    }

    // Sets the timing mode value.
    public void setTimingMode(TimingMode timingMode) {
        this.timingMode = timingMode;
    }

    // Returns the mouse button value.
    public MouseButton getMouseButton() {
        return mouseButton;
    }

    // Sets the mouse button value.
    public void setMouseButton(MouseButton mouseButton) {
        this.mouseButton = mouseButton;
    }

    // Returns the click type value.
    public ClickType getClickType() {
        return clickType;
    }

    // Sets the click type value.
    public void setClickType(ClickType clickType) {
        this.clickType = clickType;
    }

    // Returns the repeat mode value.
    public RepeatMode getRepeatMode() {
        return repeatMode;
    }

    // Sets the repeat mode value.
    public void setRepeatMode(RepeatMode repeatMode) {
        this.repeatMode = repeatMode;
    }

    // Returns the target mode value.
    public TargetMode getTargetMode() {
        return targetMode;
    }

    // Sets the target mode value.
    public void setTargetMode(TargetMode targetMode) {
        this.targetMode = targetMode;
    }

    // Returns the minimum CPS.
    public double getMinCps() {
        return minCps;
    }

    // Sets the minimum CPS.
    public void setMinCps(double minCps) {
        this.minCps = minCps;
    }

    // Returns the maximum CPS.
    public double getMaxCps() {
        return maxCps;
    }

    // Sets the maximum CPS.
    public void setMaxCps(double maxCps) {
        this.maxCps = maxCps;
    }

    // Returns the fixed timing value.
    public int getFixedValue() {
        return fixedValue;
    }

    // Sets the fixed timing value.
    public void setFixedValue(int fixedValue) {
        this.fixedValue = fixedValue;
    }

    // Returns the minimum interval in milliseconds.
    public int getMinInterval() {
        return minInterval;
    }

    // Sets the minimum interval in milliseconds.
    public void setMinInterval(int minInterval) {
        this.minInterval = minInterval;
    }

    // Returns the maximum interval in milliseconds.
    public int getMaxInterval() {
        return maxInterval;
    }

    // Sets the maximum interval in milliseconds.
    public void setMaxInterval(int maxInterval) {
        this.maxInterval = maxInterval;
    }

    // Returns the target X coordinate.
    public int getX() {
        return x;
    }

    // Sets the target X coordinate.
    public void setX(int x) {
        this.x = x;
    }

    // Returns the target Y coordinate.
    public int getY() {
        return y;
    }

    // Sets the target Y coordinate.
    public void setY(int y) {
        this.y = y;
    }

    // Copies every click setting into an independently editable instance.
    public ClickSettings copy() {
        ClickSettings copy = new ClickSettings();

        copy.setTimingMode(timingMode);
        copy.setMouseButton(mouseButton);
        copy.setClickType(clickType);
        copy.setRepeatMode(repeatMode);
        copy.setTargetMode(targetMode);

        copy.setMinCps(minCps);
        copy.setMaxCps(maxCps);
        copy.setFixedValue(fixedValue);
        copy.setMinInterval(minInterval);
        copy.setMaxInterval(maxInterval);

        copy.setX(x);
        copy.setY(y);
        copy.setRepeatAmount(repeatAmount);

        return copy;
    }

    // Describes the current settings for diagnostics.
    @Override
    public String toString() {
        return "ClickSettings{" +
                "timing=" + timingMode +
                ", button=" + mouseButton +
                ", clickType=" + clickType +
                ", repeat=" + repeatMode +
                ", repeatAmount=" + repeatAmount +
                ", target=" + targetMode +
                ", fixedValue=" + fixedValue +
                ", minCps=" + minCps +
                ", maxCps=" + maxCps +
                ", minInterval=" + minInterval +
                ", maxInterval=" + maxInterval +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
