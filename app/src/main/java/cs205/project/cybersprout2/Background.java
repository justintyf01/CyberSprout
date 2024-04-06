package cs205.project.cybersprout2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Background {
    private Bitmap bg;
    private final int screenHeight;
    private final int screenWidth;
    private Bitmap sun;
    private Bitmap moon;
    private float bodyX;
    private float bodyY;
    private boolean isDay = true;

//    public float getBodyX() {
//        return bodyX;
//    }

    public void setBodyX(float bodyX) {
        this.bodyX = bodyX;
    }

    public float getBodyY() {
        return bodyY;
    }

    public void setBodyY(float bodyY) {
        this.bodyY = bodyY;
    }

    public boolean isDay() {
        return isDay;
    }

    public void setDay(boolean day) {
        isDay = day;
    }

    public Background(Context context, int screenWidth, int screenHeight) {
        this.bg = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        this.sun = BitmapFactory.decodeResource(context.getResources(), R.drawable.sun);
        this.moon = BitmapFactory.decodeResource(context.getResources(), R.drawable.moon);

        this.bodyX = screenWidth;
        this.bodyY = (float) screenHeight / 2;
    }

    public Bitmap getBg() {
        return bg;
    }

    public void setBg(Bitmap bg) {
        this.bg = bg;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    // NOTE: THIS METHOD RETURNS SUN OR NIGHT DEPENDING ON ISDAY
    public Bitmap getBody() {
        return isDay ? sun : moon;
    }
}
