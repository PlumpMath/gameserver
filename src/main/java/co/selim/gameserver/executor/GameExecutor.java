package co.selim.gameserver.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class GameExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameExecutor.class);

    private final String name;
    private final Set<GameTask> tasks = ConcurrentHashMap.newKeySet();
    private volatile boolean connected = true;

    public GameExecutor(String name) {
        this.name = name;
        start();
    }

    private void start() {
        new Thread(() -> {
            while (connected) {
                for (Runnable task : tasks) {
                    task.run();
                }
                cleanFinishedTasks();
                try {
                    TimeUnit.MILLISECONDS.sleep(20L);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, name).start();
    }

    public void stop() {
        connected = false;
    }

    private void cleanFinishedTasks() {
        int oldSize = tasks.size();
        boolean wasCleaned = tasks.removeIf(GameTask::isDone);

        if (wasCleaned) {
            LOGGER.info("Cleaned up " + (oldSize - tasks.size()) + " tasks");
        }
    }

    public void submit(Runnable task, Supplier<Boolean> isDone) {
        GameTask gameTask = new GameTask() {
            @Override
            public boolean isDone() {
                return isDone.get();
            }

            @Override
            public void run() {
                task.run();
            }
        };
        tasks.add(gameTask);
    }

    public void submitOnce(Runnable task) {
        GameTask gameTask = new GameTask() {
            volatile boolean executed;

            @Override
            public boolean isDone() {
                return executed;
            }

            @Override
            public void run() {
                try {
                    task.run();
                } finally {
                    executed = true;
                }
            }
        };
        tasks.add(gameTask);
    }

    /**
     * Schedules a task that should only be stopped when a player disconnects.
     *
     * @param task the task to run.
     */
    public void submitConnectionBoundTask(Runnable task) {
        GameTask gameTask = new GameTask() {
            @Override
            public boolean isDone() {
                return !connected;
            }

            @Override
            public void run() {
                task.run();
            }
        };
        tasks.add(gameTask);
    }
}
