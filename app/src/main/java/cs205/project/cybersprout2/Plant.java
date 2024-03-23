package cs205.project.cybersprout2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Plant {

    private final Bitmap[] plantImages;

    public Plant(Context context) {
        plantImages = new Bitmap[2];
        plantImages[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.plant_1);
        plantImages[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.plant_2);
    }

    public Bitmap[] getPlantImages() {
        return plantImages;
    }
}
