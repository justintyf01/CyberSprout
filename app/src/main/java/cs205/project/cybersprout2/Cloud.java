package cs205.project.cybersprout2;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.content.Context;

public class Cloud {
    private final Bitmap bitmap;
    private float x; // Position
    private final float y;
    private final float speed; // Speed for horizontal movement

    private final Context context;

    public Cloud(Context context, Bitmap bitmap, float x, float y, float speed) {
        this.bitmap = bitmap;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.context = context;
    }

    public void update() {
        x += speed; // Update cloud position based on its speed

        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int cloudWidth = bitmap.getWidth(); // Assume bitmap represents the cloud image

        // For right-moving clouds
        if (speed > 0 && x > screenWidth) {
            // Cloud has moved off the right edge, reset to the left
            x = -cloudWidth;
        }
        // For left-moving clouds
        else if (speed < 0 && x + cloudWidth < 0) {
            // Cloud has moved off the left edge, reset to the right
            x = screenWidth;
        }
    }


    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, x, y, null);
    }
}
