package co.selim.gameserver.executor;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class GameExecutor {
    private final String name;
    private volatile AtomicBoolean connected;
    private final Set<Runnable> tasks = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public GameExecutor(String name, AtomicBoolean connected) {
        this.name = name;
        this.connected = connected;
        start();
    }

    private void start() {
        new Thread(() -> {
            while (connected.get()) {
                for(Runnable task : tasks) {
                    task.run();
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(20L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, name).start();
    }

    public void submit(Runnable task) {
        tasks.add(task);
    }

    public void cancel(Runnable task) {
        tasks.remove(task);
    }
}
