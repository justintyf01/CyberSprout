package cs205.project.cybersprout2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Random;

public class Fertilizer {
    private float x, y;
//    private final int color = Color.rgb(165, 42, 42); // Brown color for fertilizer
    private final Paint paint;

    public Fertilizer(Context context, float x, float y) {
        this.x = randomX(x);
        this.y = randomY(y);
        paint = new Paint();
        paint.setColor(Color.rgb(165, 42, 42));
    }

    public void draw(Canvas canvas) {
        canvas.drawCircle(x, y, 10, paint); // Draw a small brown circle
    }

    public boolean update(int maxHeight) {
        y += 25; // Move the fertilizer down on each update. Adjust the value as needed for speed.
        return !(y > maxHeight);
    }

    public float getY() {
        return y;
    }
    public float getX() {
        return x;
    }
    public float randomX(float x) {
        Random dice = new Random();
        return x + (dice.nextFloat() * 100) - 50;
    }

    public float randomY(float y) {
        Random dice = new Random();
        return y - (dice.nextFloat() * 50);
    }

}

