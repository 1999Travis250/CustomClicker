package com.travis.customclicker.model;

public class ClickSettings {

    public enum TimingMode { FIXED_INTERVAL, FIXED_CPS, RANDOM_CPS, RANDOM_INTERVAL }
    public enum MouseButton { LEFT, RIGHT, MIDDLE }
    public enum ClickType { SINGLE, DOUBLE }
    public enum RepeatMode { UNTIL_STOPPED, FIXED_AMOUNT }
    public enum TargetMode { FOLLOW_CURSOR, FIXED_POSITION }

    private TimingMode timingMode = TimingMode.FIXED_INTERVAL;
    private MouseButton mouseButton = MouseButton.LEFT;
    private ClickType clickType = ClickType.SINGLE;
    private RepeatMode repeatMode = RepeatMode.UNTIL_STOPPED;
    private TargetMode targetMode = TargetMode.FOLLOW_CURSOR;

    private double minCps = 0.5, maxCps = 1.5;
    private int fixedValue = 100, minInterval = 100, maxInterval = 300;
    private int x, y;
    private int repeatAmount = 10;

    public int getRepeatAmount() { return repeatAmount; }
    public void setRepeatAmount(int repeatAmount) { this.repeatAmount = repeatAmount; }

    public TimingMode getTimingMode() { return timingMode; }
    public void setTimingMode(TimingMode timingMode) { this.timingMode = timingMode; }

    public MouseButton getMouseButton() { return mouseButton; }
    public void setMouseButton(MouseButton mouseButton) { this.mouseButton = mouseButton; }

    public ClickType getClickType() { return clickType; }
    public void setClickType(ClickType clickType) { this.clickType = clickType; }

    public RepeatMode getRepeatMode() { return repeatMode; }
    public void setRepeatMode(RepeatMode repeatMode) { this.repeatMode = repeatMode; }

    public TargetMode getTargetMode() { return targetMode; }
    public void setTargetMode(TargetMode targetMode) { this.targetMode = targetMode; }

    public double getMinCps() { return minCps; }
    public void setMinCps(double minCps) { this.minCps = minCps; }

    public double getMaxCps() { return maxCps; }
    public void setMaxCps(double maxCps) { this.maxCps = maxCps; }

    public int getFixedValue() { return fixedValue; }
    public void setFixedValue(int fixedValue) { this.fixedValue = fixedValue; }

    public int getMinInterval() { return minInterval; }
    public void setMinInterval(int minInterval) { this.minInterval = minInterval; }

    public int getMaxInterval() { return maxInterval; }
    public void setMaxInterval(int maxInterval) { this.maxInterval = maxInterval; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

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