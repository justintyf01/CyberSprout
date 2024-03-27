package cs205.project.cybersprout2;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class Plant {
    private final Bitmap[] plantImages;
    private final int numImages = 7;
    private final AtomicInteger stage = new AtomicInteger(0);
    private final AtomicInteger saturation = new AtomicInteger(100);
    private final AtomicInteger nutrition = new AtomicInteger(100);
    private final AtomicInteger growth = new AtomicInteger();
    Resources res;
    String packageName;

    public Plant(Context context) {
        plantImages = new Bitmap[numImages];
        res = context.getResources();
        packageName = context.getPackageName();
        setPlantImages();
    }

    // don't touch
    // Getters and Setters
    public void setPlantImages() {

        for (int i = 0 ; i < numImages ; i++) {
            int resourceId = res.getIdentifier("plant" + i, "drawable", packageName);
            if (resourceId != 0) { // 0 means resource was not found
                plantImages[i] = BitmapFactory.decodeResource(res, resourceId);
            }
        }
    }

    public Bitmap getPlantImage() {
        return plantImages[stage.get()];
    }

    public int getImageWidth() {
        return plantImages[0].getWidth();
    }

    public int getImageHeight() {
        return plantImages[0].getHeight();
    }

    public int getStage() {
        return stage.get();
    }

    public void setStage(int stage) {
        this.stage.set(stage);
    }

    public void setSaturation(int saturation) {
        this.saturation.set(saturation);
    }

    public void setNutrition(int nutrition) {
        this.nutrition.set(nutrition);
    }

    public void setGrowth(int growth) {
        this.growth.set(growth);
    }

    public int getSaturation() {
        return saturation.get();
    }

    public int getNutrition() {
        return nutrition.get();
    }

    public int getGrowth() {
        return growth.get();
    }
}
