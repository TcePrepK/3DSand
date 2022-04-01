package core;

import toolbox.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ThreadManager {
    private final List<BasicThread> aliveThreadList = new ArrayList<>();
    private final List<BasicThread> threadList = new ArrayList<>();
    private final HashMap<String, BasicThread> threadMap = new HashMap<>();

    public void update() {
        for (final BasicThread thread : threadList) {
            if (thread.isAlive() && !aliveThreadList.contains(thread)) {
                aliveThreadList.add(thread);
            } else if (!thread.isAlive() && aliveThreadList.contains(thread)) {
                aliveThreadList.remove(thread);
                thread.threadDied.dispatch();
            }
        }
    }

    public BasicThread addThread(final BasicThread thread) {
        threadList.add(thread);
        threadMap.put(thread.name(), thread);

        return thread;
    }

    public BasicThread getThread(final String name) {
        final BasicThread thread = threadMap.get(name);
        if (thread == null) {
            Logger.error("Wasn't able to find thread named " + name);
        }
        return thread;
    }
}
