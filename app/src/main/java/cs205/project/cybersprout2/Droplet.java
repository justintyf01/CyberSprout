package cs205.project.cybersprout2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Random;

public class Droplet {
    private final Bitmap dropletImage;
    private final float x;
    private float y;

    public Droplet(Context context, float x, float y) {
        this.dropletImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.waterdroplet);
        this.x = randomX(x);
        this.y = y;
    }
    public boolean updateDroplet() {
        Random dice = new Random();
        this.y += 25 + (50 - 25) * dice.nextFloat();
        return !(this.y <= 0);
    }
    public float getY() {
        return y;
    }
    public float getX() {
        return x;
    }
    public float randomX(float x) {
        Random dice = new Random();
        return x + (dice.nextFloat() * 50) - 25;
//        return temp;
    }

    public Bitmap getDropletImage() {
        return dropletImage;
    }
}
