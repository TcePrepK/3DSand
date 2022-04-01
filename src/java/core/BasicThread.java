package core;

public abstract class BasicThread implements Runnable {
    private final Thread mainThread;
    private final String threadName;

    protected final Signal threadDied = new Signal();

    public BasicThread(final String name) {
        mainThread = new Thread(this, name);
        threadName = name;
    }

    @Override
    public abstract void run();

    public BasicThread start() {
        mainThread.start();
        return this;
    }

    public boolean isAlive() {
        return mainThread.isAlive();
    }

    public void whenDied(final Runnable runnable) {
        threadDied.add(runnable);
    }

    protected void breath() {
        try {
            Thread.sleep(1);
        } catch (final InterruptedException e) {
            e.printStackTrace();

            threadDied.dispatch();
        }
    }

    public String name() {
        return threadName;
    }
}
