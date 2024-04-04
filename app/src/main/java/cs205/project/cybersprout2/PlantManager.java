package cs205.project.cybersprout2;

public class PlantManager implements Runnable {
    private final Plant plant;
    private final Object pauseLock = new Object();
    private boolean isPaused = false;


    public PlantManager(Plant plant) {
        this.plant = plant;
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
                        plant.pauseGrowth();
                        pauseLock.wait();
                    }
                }
                plant.resumeGrowth();

                // get stats of plant
                int growth = plant.getGrowth();
                int saturation = plant.getSaturation();
                int nutrition = plant.getNutrition();

                // every 5 secs decrease nutrition and saturation
                if (counter % 5 == 0) {
                    plant.setNutrition(--nutrition);
                    plant.setSaturation(--saturation);
                }

                // check nutrition and saturation
                if (nutrition > 50 && saturation > 50) {
                    growth += 1;
                    plant.setGrowth(++growth);
                } else {
                    growth -= 2;
                    plant.setGrowth(growth);
                }

                // check growth
                if (growth >= 100) {
                    plant.setGrowth(0);
                    int stage = plant.getStage();
                    stage++;
                    plant.setStage(stage);
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
