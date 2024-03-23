package cs205.project.cybersprout2;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Plant {

    private final Bitmap[] plantImages;
    private final int numImages = 18;
    private int stage = 0;
    Resources res;
    String packageName;

    public Plant(Context context) {
        plantImages = new Bitmap[numImages];
        res = context.getResources();
        packageName = context.getPackageName();
        setPlantImages();

        // TODO: handles plant growth > NEED TO ENSURE MUTUAL EXCLUSION
        Thread growPlant = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        Thread.sleep(1000); // Sleep for 2 seconds
                        stage++;
                        if (stage == 18) {
                            stage = 0;
                        }
                        // use this if you want to print
//                        Log.d("UpdateThread", "Stage updated to: " + stage);
                    }
                } catch (InterruptedException e) {
                    // Handle if the thread is interrupted while sleeping
                    Thread.currentThread().interrupt(); // Restore interrupted status
                }
            }
        });
        // TODO: ensure that thread terminate gracefully
        growPlant.start();
    }

    // don't touch
    public void setPlantImages() {

        Bitmap tempBitmap = BitmapFactory.decodeResource(res, R.drawable.plant0);
        int newWidth = tempBitmap.getWidth() * 8;
        int newHeight = tempBitmap.getHeight() * 8;

        for (int i = 0 ; i < numImages ; i++) {
            int resourceId = res.getIdentifier("plant" + i, "drawable", packageName);
            if (resourceId != 0) { // 0 means resource was not found
                plantImages[i] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, resourceId), newWidth, newHeight, true);
            }
        }
    }

    public Bitmap getPlantImage() {
        return plantImages[stage];
    }

    public int getImageWidth() {
        return plantImages[0].getWidth();
    }

    public int getImageHeight() {
        return plantImages[0].getHeight();
    }

    public int getStage() {
        return stage;
    }

}
