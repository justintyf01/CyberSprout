package cs205.project.cybersprout2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BackgroundUpdater implements Runnable {
    private final Background background;
    private final long[] phaseDurations = {15000, 15000, 7500, 15000, 15000, 7500}; // Duration for each phase
    private final int[] colors = { // color of each phase
            Color.parseColor("#FF4500"), // sunrise
            Color.parseColor("#87CEEB"), // day (lightblue)
            Color.parseColor("#FF4500"), // sunset/sunrise (orange red)
            Color.parseColor("#00008B"), // night (darkblue)
            Color.parseColor("#000000"), // midnight (black)
            Color.parseColor("#00008B") // night
    };
//    private float x = 0, y = 0;
    private long startTime;
    private final int screenHeight;
    private final int screenWidth;

    // Coordinates of the body = sun || moon
    private float bodyX;
    private float bodyY;

    // Determines between sun || moon
    private boolean isDay = true;
    private long dayTime;


    public BackgroundUpdater(Background background) {

        // take in background object and update the bitmap for it in this thread
        this.background = background;
        this.startTime = System.currentTimeMillis();

        this.screenWidth = background.getScreenWidth();
        this.screenHeight = background.getScreenHeight();

        bodyX = screenWidth;
        bodyY = (float) screenHeight / 2;

        dayTime = phaseDurations[0] + phaseDurations[1] + phaseDurations[2];

    }

    // Thread to calculate background color and update sun/moon positions
    @Override
    public void run() {

        // Track start of day/night
        long bodyStartTime = System.currentTimeMillis();
        while (true) { // Ensure continuous update

            /** Calculations for progress of body (sun/moon)
             * get progress = elapsed time / 37500
             * bodyX starts from screenWidth
             * from start to 18750, need to move screenWidth + bitmapWidth = totalLength
             * bodyX = screenWidth - progress * totalLength */
            double bodyProgress = (double) (System.currentTimeMillis() - bodyStartTime) / dayTime;
            int totalLength = background.getBody().getWidth() + screenWidth;
            bodyX = (float) (screenWidth - bodyProgress * totalLength);
            background.setBodyX(bodyX);
            bodyY = calculateParabolicY(bodyX, screenHeight, screenWidth - background.getBody().getWidth());
            background.setBodyY(bodyY);

            // Change value of boolean if progress of day/night > 1 (state change)
            if (bodyProgress > 1) {
                bodyStartTime = System.currentTimeMillis();
                background.setDay(!isDay);
                isDay = !isDay;
            }


            // Calculate color and background gradient
            Bitmap bitmap = getBitmapBackground();
            // Update background bitmap
            background.setBg(bitmap);

        }
    }

    // This method calculates the gradient background
    private Bitmap getBitmapBackground() {
        long currentTime = System.currentTimeMillis() - startTime;
        long totalPhaseDuration = getTotalPhaseDuration();
        long currentPhaseTime = currentTime % totalPhaseDuration;

        // Calculate the current phase based on the elapsed time
        int phase = 0;
        long timeSum = 0;
        for (int i = 0; i < phaseDurations.length; i++) {
            timeSum += phaseDurations[i];
            if (currentPhaseTime < timeSum) {
                phase = i;
                break;
            }
        }

        // Calculate progress
        long phaseStartTime = timeSum - phaseDurations[phase];
        float phaseProgress = (currentPhaseTime - phaseStartTime) / (float) phaseDurations[phase];

        // Handle looping of phases
        int startColor = colors[phase];
        int endColor;
        if (phase == colors.length - 1) {
            endColor = colors[0];
        } else {
            endColor = colors[phase + 1];
        }

        // Calculate current color based on progress
        int newColor = interpolateColor(startColor, endColor, phaseProgress);
        Bitmap bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        for (int y = 0; y < screenHeight; y++) {
            for (int x = 0; x < screenWidth; x++) {
                // Calculate gradient
                int gradientColor = calculateGradientColor(newColor, y);
                // Assign gradient bitmap
                bitmap.setPixel(x, y, gradientColor);
            }
        }


        // TODO: supposed to reset startTime so its not forever running but this line is wrong
        startTime %= getTotalPhaseDuration();

        // Return bitmap of running time progress background calculated
        return bitmap;
    }

    // Calculate current color based on progress
    private int interpolateColor(int colorStart, int colorEnd, float progress) {
        int alphaStart = Color.alpha(colorStart);
        int redStart = Color.red(colorStart);
        int greenStart = Color.green(colorStart);
        int blueStart = Color.blue(colorStart);

        int alphaEnd = Color.alpha(colorEnd);
        int redEnd = Color.red(colorEnd);
        int greenEnd = Color.green(colorEnd);
        int blueEnd = Color.blue(colorEnd);

        int alpha = (int) (alphaStart + (alphaEnd - alphaStart) * progress);
        int red = (int) (redStart + (redEnd - redStart) * progress);
        int green = (int) (greenStart + (greenEnd - greenStart) * progress);
        int blue = (int) (blueStart + (blueEnd - blueStart) * progress);

        return Color.argb(alpha, red, green, blue);
    }

    // Calculate gradient based on current color calculated by interpolateColor
    private int calculateGradientColor(int color, int y) {
        float gradientFactor = (float) y / screenHeight; // 0 at top, 1 at bottom
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        // Adjust the color's brightness based on its position
        // For a stronger effect at the top, we decrease brightness towards the bottom
        float brightnessFactor = 1 - gradientFactor * 0.5f; // Adjust this factor as needed

        red = (int) (red * brightnessFactor);
        green = (int) (green * brightnessFactor);
        blue = (int) (blue * brightnessFactor);

        return Color.argb(alpha, red, green, blue);
    }
    // use for calculation parabola of sun/moon
    private float calculateParabolicY(float x, int screenHeight, int screenWidth) {
        // Parabolic trajectory: y = -4*a*(x - p)^2 + q; where a controls the width, p is the peak's x-position, q is the peak's y-position.
        float a = 1000f / (screenWidth * screenWidth); // Adjust 'a' as needed
        float p = screenWidth / 2.0f; // Peak at the middle of the screen
        float q = screenHeight / 5.0f; // Adjust 'q' to set the peak's height, 1/3rd from the top
        return a * (x - p) * (x - p) + q;
    }

    private long getTotalPhaseDuration() {
        long total = 0;
        for (long duration : phaseDurations) {
            total += duration;
        }
        return total;
    }
}

