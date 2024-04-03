package cs205.project.cybersprout2;

public class GameThread extends Thread {

    private final Game game;
    private volatile boolean isRunning = false;
    private final Object pauseLock = new Object();
    private boolean isPaused = false;
    private final PlantManager plantManager;

    public GameThread(Game game) {
        this.game = game;
        this.plantManager = new PlantManager(game.getPlant());
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
        while (isRunning) {
            synchronized (pauseLock) {
                if (isPaused) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            game.draw();
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
