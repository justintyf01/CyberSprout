package cs205.project.cybersprout2;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Plant {

    private final Bitmap[] plantImages;
    private final int numImages = 18;

    Resources res;
    String packageName;

    public Plant(Context context) {
        plantImages = new Bitmap[numImages];
        res = context.getResources();
        packageName = context.getPackageName();
        setPlantImages();
    }

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

    public Bitmap[] getPlantImages() {
        return plantImages;
    }
}
