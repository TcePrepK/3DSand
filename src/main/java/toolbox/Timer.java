package toolbox;

public class Timer {
    private double startTime = 0;
    private boolean counting = false;

    public void startTimer() {
        if (counting) {
            stopTimer();
        }

        startTime = Timer.getCurrentTime();
        counting = true;
    }

    public double stopTimer() {
        final double time = (Timer.getCurrentTime() - startTime) / (double) 1000;

        counting = false;
        startTime = 0;

        return time;
    }

    private static long getCurrentTime() {
        return System.currentTimeMillis();
//        return Sys.getTime() * 1000 / Sys.getTimerResolution();
    }
}
