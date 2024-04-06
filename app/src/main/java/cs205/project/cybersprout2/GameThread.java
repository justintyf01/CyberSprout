package cs205.project.cybersprout2;

public class GameThread extends Thread {

    private final Game game;
    private volatile boolean isRunning = false;
    private final Object pauseLock = new Object();
    private boolean isPaused = false;

    // fps
    private static final long FPS_INTERVAL = 1000;
    private long frameCount = 0;
    private long startTime = 0;
    private long fps = 0;

    public GameThread(Game game) {
        this.game = game;
    }

    public void startGame() {
        isRunning = true;

        start();
    }

    public void stopGame() {
        isRunning = false;
        // Ensure we also resume the thread so it can finish properly
        resumeGame();
    }

    @Override
    public void run() {
        while (isRunning && game.isGameReady()) {

            // for pause
            synchronized (pauseLock) {
                if (isPaused) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            long currentTime = System.currentTimeMillis();
            if (startTime == 0) {
                startTime = currentTime;
            }

            // Calculate FPS
            frameCount++;
            if (currentTime - startTime >= FPS_INTERVAL) {
                fps = (long) (frameCount / ((currentTime - startTime) / 1000.0));
                game.setFps(fps);
                frameCount = 0;
                startTime = currentTime;
            }

            game.draw();

            // sleep to control fps
            try {
                sleep(8);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void pauseGame() {
        synchronized (pauseLock) {
            isPaused = true;
            game.pauseGame(); // Pause plant growth
        }
    }

    public void resumeGame() {
        synchronized (pauseLock) {
            isPaused = false;
            pauseLock.notifyAll();
            game.resumeGame(); // Resume plant growth
        }
    }
    public boolean isPaused() {
        return isPaused;
    }
}
