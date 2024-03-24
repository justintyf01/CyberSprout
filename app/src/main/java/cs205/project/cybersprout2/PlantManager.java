package cs205.project.cybersprout2;

public class PlantManager implements Runnable {
    private final Plant plant;

    PlantManager(Plant plant) {
        this.plant = plant;
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

                // get stats of plant
                int growth = plant.getGrowth();
                int saturation = plant.getSaturation();
                int nutrition = plant.getNutrition();
//                System.out.printf("growth = %d, saturation = %d. nutrition = %d", growth, saturation, nutrition);

                // every 5 secs decrease nutrition and saturation
                if (counter % 50 == 0) {
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
                Thread.sleep(100); // Sleep for 0.1 seconds
            }

        } catch (InterruptedException e) {
            // Handle if the thread is interrupted while sleeping
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
    }
}
