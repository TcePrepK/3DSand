package toolbox;

import core.DisplayManager;

public class Timer {
    private double startTime = 0;
    private boolean counting = false;

    public void startTimer() {
        if (counting) {
            stopTimer();
        }

        startTime = DisplayManager.getCurrentTime();
        counting = true;
    }

    public double stopTimer() {
        final double time = (DisplayManager.getCurrentTime() - startTime) / (double) 1000;

        counting = false;
        startTime = 0;

        return time;
    }
}
