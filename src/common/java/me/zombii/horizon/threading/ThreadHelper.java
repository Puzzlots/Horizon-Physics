package me.zombii.horizon.threading;

import com.badlogic.gdx.utils.PauseableThread;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ThreadHelper {

    private static final Map<String, Thread> threadMap = new HashMap<>();

    public static Thread create(String name, Runnable runnable) {
        Thread thread = new Thread(runnable, name);
        threadMap.put(name, thread);
        return thread;
    }

    public static PauseableThread createPauseable(String name, Runnable runnable) {
        PauseableThread thread = new PauseableThread(runnable);
        thread.setName(name);
        threadMap.put(name, thread);
        return thread;
    }

    public static PauseableThread createTicking(String name, TickingRunnable runnable) {
        return createPauseable(name, new RunnableTicker(runnable));
    }

    public static void addThread(Thread thread) {
        threadMap.put(thread.getName(), thread);
    }

    public static @Nullable Thread getThread(String name) {
        return threadMap.get(name);
    }

    public static boolean exist(String name) {
        return threadMap.containsKey(name);
    }

    public static void killAll() {
        for (Thread thread : threadMap.values()) {
            if (thread instanceof PauseableThread) {
                ((PauseableThread) thread).onPause();
                ((PauseableThread) thread).stopThread();
            } else {
//                thread.interrupt();
                // noinspection all
                thread.stop();
            }
        }
    }

}
