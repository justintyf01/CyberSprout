package cs205.project.cybersprout2;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class WateringCan {

    private final float x;
    private final float y;
    private Bitmap wateringCanImage;
    private Resources res;
    public WateringCan(Context context, float x, float y) {
        res = context.getResources();
        this.x = x;
        this.y = y;
        wateringCanImage = BitmapFactory.decodeResource(res, R.drawable.wateringcan);
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
