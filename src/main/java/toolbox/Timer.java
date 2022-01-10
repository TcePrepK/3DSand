package toolbox;

import core.DisplayManager;

public class Timer {
    private float startTime;
    private boolean counting = false;

    public void startTimer() {
        if (counting) {
            stopTimer();
        }

        startTime = DisplayManager.getCurrentTime();
        counting = true;
    }

    public float stopTimer() {
        counting = false;
        startTime = 0;

        return (DisplayManager.getCurrentTime() - startTime) / 1000f;
    }
}
