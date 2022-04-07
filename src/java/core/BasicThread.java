package core;

public abstract class BasicThread implements Runnable {
    private final Thread mainThread;
    private final String threadName;

    protected final Signal threadDied = new Signal();
    protected boolean isDead = false;

    private final Timer loopTimer = new Timer();
    private float threadAliveTime = 0;
    private int loopTime = 0;
    private float threadFrame = 0;

    public BasicThread(final String name) {
        mainThread = new Thread(this, name);
        threadName = name;
    }

    @Override
    public void run() {
        while (!isDead) {
            threadFrame++;
            loopTimer.startTimer();

            loop();
            breath();

            final double delta = loopTimer.stopTimer();
            loopTime = (int) Math.floor(delta * 1000);

            threadAliveTime += delta;
        }
    }

    protected abstract void loop();

    public BasicThread start() {
        mainThread.start();
        return this;
    }

    public void whenDied(final Runnable runnable) {
        threadDied.add(runnable);
    }

    protected void breath() {
        try {
            Thread.sleep(0);
        } catch (final InterruptedException e) {
            e.printStackTrace();
            kill();
        }
    }

    protected void kill() {
        isDead = true;
    }

    public boolean isDead() {
        return isDead;
    }

    public String name() {
        return threadName;
    }

    public float getLoopTime() {
        return loopTime;
    }

    public float getThreadAliveTime() {
        return threadAliveTime;
    }
}
