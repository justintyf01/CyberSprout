package cs205.project.cybersprout2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BackgroundUpdater implements Runnable {
    private final Background background;
    private final long[] phaseDurations = {15000, 7500, 15000, 15000, 7500}; // Duration for each phase
    private final int[] colors = { // color of each phase
            Color.parseColor("#87CEEB"), // day (lightblue)
            Color.parseColor("#FF4500"), // sunset/sunrise (orange red)
            Color.parseColor("#00008B"), // night (darkblue)
            Color.parseColor("#000000"), // midnight (black)
            Color.parseColor("#FF4500")
    };
    private float x = 0, y = 0;
    private long startTime;
    private final int screenHeight;
    private final int screenWidth;
//    private final Context context;


    public BackgroundUpdater(Background background) {

        this.background = background;
        this.startTime = System.currentTimeMillis();

        this.screenWidth = background.getScreenWidth();
        this.screenHeight = background.getScreenHeight();

    }

    @Override
    public void run() {
        while (true) { // Ensure continuous update
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

            int newColor = interpolateColor(startColor, endColor, phaseProgress);
            Bitmap bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
            for (int y = 0; y < screenHeight; y++) {
                for (int x = 0; x < screenWidth; x++) {
                    int gradientColor = calculateGradientColor(newColor, y);
                    bitmap.setPixel(x, y, gradientColor);
                }
            }

            background.setBg(bitmap);
            if (endColor == colors[0]) {
                startTime = System.currentTimeMillis();
            }

        }
    }

    // use for calculation parabola of sun/moon
    private float calculateParabolicY(float x, int screenHeight, int screenWidth) {
        // Parabolic trajectory: y = -4*a*(x - p)^2 + q; where a controls the width, p is the peak's x-position, q is the peak's y-position.
        float a = 4.0f / (screenWidth * screenWidth); // Adjust 'a' as needed
        float p = screenWidth / 2.0f; // Peak at the middle of the screen
        float q = screenHeight / 3.0f; // Adjust 'q' to set the peak's height, 1/3rd from the top
        return -a * (x - p) * (x - p) + q;
    }

    private long getTotalPhaseDuration() {
        long total = 0;
        for (long duration : phaseDurations) {
            total += duration;
        }
        return total;
    }

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
}

