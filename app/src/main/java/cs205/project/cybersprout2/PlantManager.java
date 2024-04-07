package cs205.project.cybersprout2;

import java.util.Random;

public class PlantManager implements Runnable {
    private final Plant plant;
    private final Object pauseLock = new Object();
    private boolean isPaused = false;
    private final GameResultListener gameResultListener;

    public PlantManager(Plant plant, GameResultListener gameResultListener) {
        this.plant = plant;
        this.gameResultListener = gameResultListener; // Initialize it here
    }

    public void pause() {
        synchronized (pauseLock) {
            isPaused = true;
        }
    }

    public void resume() {
        synchronized (pauseLock) {
            isPaused = false;
            pauseLock.notifyAll(); // Notify to resume operation
        }
    }

    @Override
    public void run() {
        try {
            int counter = 0;
            while (!Thread.currentThread().isInterrupted()) {

                // reset counter
                if (counter == Integer.MAX_VALUE) {
                    counter = 0;
                }

                // Check if the game is paused
                synchronized (pauseLock) {
                    while (isPaused) {
                        pauseLock.wait();
                    }
                }

                // get stats of plant
                int growth = plant.getGrowth();
                int saturation = plant.getSaturation();
                int nutrition = plant.getNutrition();

                // every 5 secs decrease nutrition and saturation
                if (counter % 3 == 0) {
                    Random dice = new Random();
                    plant.setNutrition(nutrition - dice.nextInt(10));
                    plant.setSaturation(saturation - dice.nextInt(10));
//                    plant.setNutrition(--nutrition);
//                    plant.setSaturation(--saturation);
                }

                // check nutrition and saturation
                if (nutrition > 50 && saturation > 50) {
                    growth += 5;
                    plant.setGrowth(growth);
                } else {
                    growth -= 3;
                    plant.setGrowth(growth);
                }

                // check growth
                if (growth >= 100) {
                    plant.setGrowth(0);
                    int stage = plant.getStage();
                    stage++;
                    if (stage == 7) {
                        gameResultListener.onWin(); // Win after surpassing last stage
                        return;
                    }
                    plant.setStage(stage);
                    gameResultListener.currentStage(stage);

                } else if (growth < 0) {
                    int stage = plant.getStage();
                    if (stage <= 0 ){
                        plant.setGrowth(0); // Ensure growth doesn't go below zero
                        gameResultListener.onGameOver(); // Notify the Game class
                        return;

                    } else {
                        plant.setGrowth(100);
                        stage--;
                        plant.setStage(stage);
                        gameResultListener.currentStage(stage);
                    }
                }

                counter++;
                Thread.sleep(1000); // Sleep for 1 seconds

            }
        } catch (InterruptedException e) {
            // Handle if the thread is interrupted while sleeping
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
    }
}
