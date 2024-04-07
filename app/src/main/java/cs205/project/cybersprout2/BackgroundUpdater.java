package cs205.project.cybersprout2;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class BackgroundUpdater implements Runnable {
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final Background background;
    private final long dayDuration = 120000; // Duration for a full day-night cycle in milliseconds
    private final int screenHeight;
    private final int screenWidth;
    private long startTime;
    private long pauseStartTime = 0; // Track when the pause started
    private long totalPauseDuration = 0; // Total duration of all pauses
    private List<Star> stars;

    public BackgroundUpdater(Background background) {
        this.background = background;
        this.screenWidth = background.getScreenWidth();
        this.screenHeight = background.getScreenHeight();
        this.startTime = System.currentTimeMillis();
        initializeStars();
    }

    private void initializeStars() {
        stars = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < 100; i++) { // Generate 100 stars at random positions
            int x = rand.nextInt(screenWidth);
            int y = rand.nextInt(screenHeight);
            stars.add(new Star(x, y));
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if (paused.get()) {
                synchronized (paused) {
                    try {
                        paused.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - startTime - totalPauseDuration;
            boolean isDay = elapsedTime / dayDuration % 2 == 0;
            Bitmap updatedBackground = createBackgroundBitmap(elapsedTime % dayDuration, isDay);
            background.setBg(updatedBackground);

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private Bitmap createBackgroundBitmap(long elapsedTime, boolean isDay) {
        Bitmap bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        float progress = (float) elapsedTime / dayDuration;
        // Adjust for a smoother transition based on the sun's position
        int backgroundColor = interpolateBackgroundColor(progress, isDay);
        canvas.drawColor(backgroundColor);

        drawCelestialBody(canvas, progress, isDay);

        if (!isDay) {
            drawStars(canvas, elapsedTime);
        }

        return bitmap;
    }


    private int interpolateBackgroundColor(float progress, boolean isDay) {
        // Define colors for sunrise, brightest day, sunset, and darkest night
        int sunriseColor = Color.parseColor("#FFDAB9");    // Sunrise color (Orange)
        int brightestDayColor = Color.parseColor("#87CEEB"); // Brightest day color (Sky Blue)
        int sunsetColor = Color.parseColor("#663399");      // Sunset color (Orange Red)
        int darkestNightColor = Color.parseColor("#000000"); // Darkest night color (Dark Blue)

        // Calculate the background color based on isDay and progress
        if (isDay) {
            // Daytime background interpolation
            if (progress <= 0.5f) {
                // Interpolate from sunrise to brightest day color
                return interpolateColors(sunriseColor, brightestDayColor, progress / 0.5f);
            } else {
                // Interpolate from brightest day color to sunset
                return interpolateColors(brightestDayColor, sunsetColor, (progress - 0.5f) / 0.5f);
            }
        } else {
            // Nighttime background interpolation
            if (progress <= 0.5f) {
                // Interpolate from sunset to darkest night color
                return interpolateColors(sunsetColor, darkestNightColor, progress / 0.5f);
            } else {
                // Interpolate from darkest night color to sunrise
                return interpolateColors(darkestNightColor, sunriseColor, (progress - 0.5f) / 0.5f);
            }
        }
    }


    // Helper method to interpolate between two colors based on a factor (0 to 1)
    private int interpolateColors(int colorStart, int colorEnd, float factor) {
        int startA = (colorStart >> 24) & 0xff;
        int startR = (colorStart >> 16) & 0xff;
        int startG = (colorStart >> 8) & 0xff;
        int startB = colorStart & 0xff;

        int endA = (colorEnd >> 24) & 0xff;
        int endR = (colorEnd >> 16) & 0xff;
        int endG = (colorEnd >> 8) & 0xff;
        int endB = colorEnd & 0xff;

        int interpolatedA = (int) (startA + (endA - startA) * factor);
        int interpolatedR = (int) (startR + (endR - startR) * factor);
        int interpolatedG = (int) (startG + (endG - startG) * factor);
        int interpolatedB = (int) (startB + (endB - startB) * factor);

        return (interpolatedA << 24) | (interpolatedR << 16) | (interpolatedG << 8) | interpolatedB;
    }

    // sun peaks at progress = 0.5 now
    private void drawCelestialBody(Canvas canvas, float progress, boolean isDay) {
        // Calculate the position of the sun/moon
    //    float xPosition = screenWidth * (1 - progress); // Invert direction for sun/moon rise
    //    float yPosition = (float) (screenHeight * 0.5 * (1 - Math.cos(Math.PI * progress)));

        float x_center = screenWidth / 2; // Center of the screen horizontally
        float y_center = screenHeight; // Center of the screen vertically
        float r = (float) (screenHeight * 0.8); // You need to define the radius of the circular path

        // Calculate the angle theta based on progress (progress ranges from 0 to 1 for a full cycle)
        float theta = (float) ((progress + 0.25) * 2 * Math.PI); // Progress converted to radians

        // Calculate the x and y coordinates of the sun
        float xPosition = x_center + r * (float) Math.cos(theta);
        float yPosition = y_center + r * (float) Math.sin(theta);

        // Get the appropriate celestial body bitmap from the background object
        Bitmap celestialBodyBitmap = background.getBody();

        // Calculate the size of the celestial body
        int width = celestialBodyBitmap.getWidth();
        int height = celestialBodyBitmap.getHeight();

        // Calculate the position where the bitmap will be drawn
        float left = xPosition - width / 2;
        float top = yPosition - height / 2;

        // Draw the bitmap at the calculated position
        canvas.drawBitmap(celestialBodyBitmap, left, top, null);

        // Update the Background class with the current celestial body position
        background.setBodyX(left + celestialBodyBitmap.getWidth() / 2);
        background.setBodyY(top + celestialBodyBitmap.getHeight() / 2);
        background.setDay(isDay);
    }

    private void drawStars(Canvas canvas, long elapsedTime) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        float duskProgress = Math.max(0, 1 - 4 * (float) elapsedTime / dayDuration); // Reverse progress during dusk

        for (Star star : stars) {
            // Fade in stars based on dusk progress
            int alpha = duskProgress < 1 ? (int)(255 * (1 - duskProgress)) : 255;
            paint.setAlpha(alpha);

            float x = (star.x + elapsedTime / 20) % screenWidth;
            canvas.drawCircle(x, star.y, 2, paint);
        }
    }

    public void setPaused(boolean shouldPause) {
        if (shouldPause) {
            paused.set(true);
            pauseStartTime = System.currentTimeMillis(); // Mark the pause start time
        } else {
            if (paused.getAndSet(false)) { // Ensure we only calculate if it was previously paused
                totalPauseDuration += (System.currentTimeMillis() - pauseStartTime); // Update total pause duration
            }
            synchronized (paused) {
                paused.notifyAll();
            }
        }
    }

    static class Star {
        final int x;
        final int y;

        Star(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
