package cs205.project.cybersprout2;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class WateringCan {

    private final float x;
    private final float y;
    private Bitmap wateringCanImage;
    private Resources res;
    public WateringCan(Context context, float x, float y) {
        res = context.getResources();

        Bitmap originalBitmap = BitmapFactory.decodeResource(res, R.drawable.wateringcan);
        this.x = x - (int)originalBitmap.getWidth();
        this.y = y - (int)originalBitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postRotate(-45); // Rotate 45 degrees to the left
        wateringCanImage = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
    }

    public Bitmap getWateringCanImage() {
        return wateringCanImage;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

}
