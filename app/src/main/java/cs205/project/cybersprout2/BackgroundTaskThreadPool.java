package cs205.project.cybersprout2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackgroundTaskThreadPool {
    final ExecutorService pool;
    private final static BackgroundTaskThreadPool backgroundTaskThreadPool = new BackgroundTaskThreadPool();
    private BackgroundTaskThreadPool() {
        final int cpuCores = Math.max(Runtime.getRuntime().availableProcessors() - 1, 1);

        pool = Executors.newFixedThreadPool(cpuCores);
    }

    public static BackgroundTaskThreadPool getThreadPool() {
        return backgroundTaskThreadPool;
    }

    public void submit(final Runnable task) {
        pool.submit(task);
    }
}
