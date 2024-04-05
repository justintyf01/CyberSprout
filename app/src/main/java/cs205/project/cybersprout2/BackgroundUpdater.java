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
    private final long dayDuration = 30000; // Duration for a full day-night cycle in milliseconds
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
            int y = rand.nextInt(screenHeight / 2); // Limit stars to the top half of the screen
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
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private Bitmap createBackgroundBitmap(long elapsedTime, boolean isDay) {
        Bitmap bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Interpolate the background color
        float progress = (float) elapsedTime / dayDuration;
        int backgroundColor = interpolateBackgroundColor(progress, isDay);
        canvas.drawColor(backgroundColor);

        // Draw sun or moon
        drawCelestialBody(canvas, progress, isDay);

        // Draw stars if it's night
        if (!isDay) {
            drawStars(canvas, elapsedTime);
        }

        return bitmap;
    }

    private int interpolateBackgroundColor(float progress, boolean isDay) {
        // Define colors for day and night
        int dayColor = Color.parseColor("#87CEEB");
        int nightColor = Color.parseColor("#000033");


        // Calculate interpolation factor
        float factor = isDay ? progress : 1 - progress;

        // Smooth transition near sunrise and sunset
        factor = (float) Math.cos(factor * Math.PI) / 2.0f + 0.5f;

        return Color.rgb(
                (int) (Color.red(dayColor) * factor + Color.red(nightColor) * (1 - factor)),
                (int) (Color.green(dayColor) * factor + Color.green(nightColor) * (1 - factor)),
                (int) (Color.blue(dayColor) * factor + Color.blue(nightColor) * (1 - factor))
        );
    }

//    private void drawCelestialBody(Canvas canvas, float progress, boolean isDay) {
//        Paint paint = new Paint();
//        float xPosition = screenWidth * (1 - progress); // Invert direction for sun/moon rise
//        float yPosition = (float) (screenHeight * 0.5 * (1 - Math.cos(Math.PI * progress)));
//
//        int color = isDay ? Color.YELLOW : Color.LTGRAY;
//        int radius = isDay ? 80 : 100; // Bigger sun and moon
//
//        paint.setColor(color);
//        canvas.drawCircle(xPosition, yPosition, radius, paint);
//    }
private void drawCelestialBody(Canvas canvas, float progress, boolean isDay) {
    // Calculate the position of the sun/moon
    float xPosition = screenWidth * (1 - progress); // Invert direction for sun/moon rise
    float yPosition = (float) (screenHeight * 0.5 * (1 - Math.cos(Math.PI * progress)));

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
                totalPauseDuration += System.currentTimeMillis() - pauseStartTime; // Update total pause duration
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
