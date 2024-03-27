package cs205.project.cybersprout2;

import android.content.Context;
import android.graphics.Bitmap;

public class Background {
    private Bitmap bg;
    private final int screenHeight;
    private final int screenWidth;

    public Background(Context context, int screenWidth, int screenHeight) {
        this.bg = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
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
}
