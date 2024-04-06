package cs205.project.cybersprout2;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Paint;
import android.view.MotionEvent;

public class Game {
    /************************** SYSTEM **************************/
    private final Context context;
    private final Predicate<Consumer<Canvas>> useCanvas;
    private final int screenHeight;

    public BackgroundUpdater updater;
    /************************* BACKGROUND *************************/
    private final Background background;
    private long startTime = System.currentTimeMillis();
    private AtomicInteger currentColor = new AtomicInteger(Color.parseColor("#87CEEB"));
    private boolean isSun;
    private final Bitmap sun;
    private final Bitmap moon;
    private float bodyX;
    private float bodyY;

    /*************************** PLANT ***************************/
    private final Plant plant;
    private final PlantManager plantManager;

    /****************** WATERING CAN AND DROPLETS ******************/
    private final Map<Integer, TouchObject> activeTouches = new HashMap<>();
    private WateringCan wateringCan;
    private FertilizerBox fertilizerBox;
    private final List<Fertilizer> fertilizers = new ArrayList<>();
    private final ReentrantLock fertilizerLock = new ReentrantLock();
    private final List<Droplet> droplets = new ArrayList<>();
    private final ReentrantLock dropletLock = new ReentrantLock();
    private final ExecutorService dropletExecutorService;
    private volatile boolean isPaused = false;
    private final ExecutorService fertilizerExecutorService;
    // private BackgroundTaskThreadPool threadPool = BackgroundTaskThreadPool.getThreadPool();

    /****************** STATUS BAR ******************/
    private final Bitmap growthIcon;
    private final Bitmap nutritionIcon;
    private final Bitmap saturationIcon;
    private final Bitmap pauseIcon;
    private final Bitmap pausedBanner;
    private final Bitmap playIcon;

    private final Bitmap cloud4;
    private final List<Cloud> clouds = new ArrayList<>();
    private final boolean cloudsScaled = false;
    private boolean isGameReady = false;
    private BackgroundUpdater backgroundUpdater;

    // fps
    private Paint fpsPaint;
    private long fps = 0;

    public void setFps(long fps) {
        this.fps = fps;
    }

    public Game(Context context, final Predicate<Consumer<Canvas>> useCanvas) {
        // add this to the parameter if implementing notifications
//        this.runnable = runnable;
        this.context = context;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        this.screenHeight = context.getResources().getDisplayMetrics().heightPixels;

        this.background = new Background(context, screenWidth, screenHeight);
        // Start thread to constantly update background values based on real time
        this.backgroundUpdater = new BackgroundUpdater(background);
        new Thread(backgroundUpdater, "BackgroundUpdater").start();
        this.useCanvas = useCanvas;
        plant = new Plant(context);
        this.dropletExecutorService = Executors.newFixedThreadPool(1);
        this.fertilizerExecutorService = Executors.newFixedThreadPool(1);

        this.sun = BitmapFactory.decodeResource(context.getResources(), R.drawable.sun);
        this.moon = BitmapFactory.decodeResource(context.getResources(), R.drawable.moon);

        int logoSize = 80;

        Bitmap originalGrowthIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.growth_icon);
        Bitmap originalSaturationIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.saturation_icon);
        Bitmap originalNutritionIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.nutrition_icon);
        Bitmap originalPauseIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.pause_button);
        Bitmap originalPlayIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.play_button);
        Bitmap originalPausedBanner = BitmapFactory.decodeResource(context.getResources(), R.drawable.paused_banner);

        this.growthIcon = Bitmap.createScaledBitmap(originalGrowthIcon, logoSize, logoSize, true);
        this.saturationIcon = Bitmap.createScaledBitmap(originalSaturationIcon, logoSize, logoSize, true);
        this.nutritionIcon = Bitmap.createScaledBitmap(originalNutritionIcon, logoSize, logoSize, true);
        this.pauseIcon = Bitmap.createScaledBitmap(originalPauseIcon, logoSize, logoSize, true);
        this.playIcon = Bitmap.createScaledBitmap(originalPlayIcon, logoSize, logoSize, true);
        this.pausedBanner = Bitmap.createScaledBitmap(originalPausedBanner, originalPausedBanner.getWidth(), originalPausedBanner.getHeight(), true);

        int cloudWidth = 200; // Desired width for the cloud images
        int cloudHeight = 150; // Desired height for the cloud images

        Bitmap cloud1Bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud1);
        /****************** CLOUDS ******************/
        Bitmap cloud1 = Bitmap.createScaledBitmap(cloud1Bitmap, cloudWidth, cloudHeight, false);

        Bitmap cloud2Bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud2);
        Bitmap cloud2 = Bitmap.createScaledBitmap(cloud2Bitmap, cloudWidth, cloudHeight, false);

        Bitmap cloud3Bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud3);
        Bitmap cloud3 = Bitmap.createScaledBitmap(cloud3Bitmap, cloudWidth, cloudHeight, false);

        Bitmap cloud4Bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud4);
        this.cloud4 = Bitmap.createScaledBitmap(cloud4Bitmap, cloudWidth, cloudHeight, false);


        clouds.add(new Cloud(context, cloud1, 0, 200, -1.2f)); // Cloud 1
        clouds.add(new Cloud(context, cloud2, screenWidth, 100, 1.1f)); // Cloud 2
        clouds.add(new Cloud(context, cloud3, (float) screenWidth / 2, 300, 0.9f)); // Cloud 3
        this.plantManager = new PlantManager(plant);
        new Thread(plantManager, "PlantManager").start();
        isGameReady = true;
    }

    // maybe can refactor this method with the fertilizer box method
    public void handleWateringCanTouch(int pointerId, float x, float y, boolean isDown) {
        if (isPaused) return;
        if (isDown) {
//            activeTouches.put(pointerId, new PointF(x, y));
            this.wateringCan = new WateringCan(context, x, y);
            activeTouches.put(pointerId, wateringCan);

            dropletExecutorService.execute(() -> {
                while (activeTouches.get(pointerId) != null) {
                    if (wateringCan != null) {
                        int plantSaturation = plant.getSaturation();
                        plant.setSaturation(plantSaturation >= 100 ? 100 : plantSaturation + 1);
                        dropletLock.lock();
                        try {
                            float dropletX = wateringCan.getX() + 100; // This line was causing the crash
                            float dropletY = wateringCan.getY() + 425;
                            droplets.add(new Droplet(context, dropletX, dropletY));
                            droplets.add(new Droplet(context, dropletX, dropletY));
                            droplets.add(new Droplet(context, dropletX, dropletY));

                            // Other operations
                        } finally {
                            dropletLock.unlock();
                        }
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        } else {
            // If the finger is lifted, remove the touch point
            activeTouches.remove(pointerId);
            wateringCan = null;

//            wateringCan.endThread();
        }
    }

    public void pauseGame() {
        isPaused = true;
        // Pause droplet and fertilizer tasks
        // Note: You'll need to implement a mechanism to pause/resume tasks within these executors or manage tasks directly
        plantManager.pause();
        backgroundUpdater.setPaused(true);
    }
    public void resumeGame() {
        isPaused = false;
        // Resume droplet and fertilizer tasks
        plantManager.resume();
        backgroundUpdater.setPaused(false);
    }

    public void handleFertiliserTouch (int pointerId, float x, float y, boolean isDown) {
        if (isPaused) return;
        if (isDown) {
            this.fertilizerBox = new FertilizerBox(context, x, y); // Assuming this sets the initial position
            activeTouches.put(pointerId, fertilizerBox);

            fertilizerExecutorService.execute(() -> {
                while (activeTouches.get(pointerId) != null) {
                    int plantNutrition = plant.getNutrition();
                    plant.setNutrition(plantNutrition >= 100 ? 100 : plantNutrition + 1);
                    fertilizerLock.lock();
                    try {
                        float fertilizerX = fertilizerBox.getX() + 250; // Starting X position
                        float fertilizerY = fertilizerBox.getY() + 175; // Starting Y position

//                        Fertilizer fertilizer = new Fertilizer(context, fertilizerX, fertilizerY);
//                        fertilizers.add(new Fertilizer(context, fertilizerX, fertilizerY));
//                        fertilizers.add(new Fertilizer(context, fertilizerX, fertilizerY));
                        fertilizers.add(new Fertilizer(context, fertilizerX, fertilizerY));
                        fertilizers.add(new Fertilizer(context, fertilizerX, fertilizerY));
                        fertilizers.add(new Fertilizer(context, fertilizerX, fertilizerY));

                    } finally {
                        fertilizerLock.unlock();
                    }
                    try {
                        Thread.sleep(100); // Control the speed of falling
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        } else {
            activeTouches.remove(pointerId);
            fertilizerBox = null;
        }
    }
    public boolean isGameReady(){
        return isGameReady;
    }
    public void draw() {
        if (!isGameReady) return;
        if (isPaused) return;

        // Initialize paint for FPS counter
        fpsPaint = new Paint();
        fpsPaint.setColor(Color.WHITE);
        fpsPaint.setTextSize(40);

        useCanvas.test(this::draw);
    }

    // this method does the actual drawing
    public void draw(Canvas canvas) {
        if (canvas == null) {
            return;
        }
        canvas.drawBitmap(background.getBg(), 0,0,null);
        //canvas.drawBitmap(background.getBody(), background.getBodyX(), background.getBodyY(), null);
        for (TouchObject obj : activeTouches.values()) {
            canvas.drawBitmap(obj.getImage(), obj.getX(), obj.getY(), null);
        }

        dropletLock.lock();
        if (!droplets.isEmpty()) {
            Iterator<Droplet> dropletIterator = droplets.iterator();
            while (dropletIterator.hasNext()) {
                Droplet d = dropletIterator.next();
                canvas.drawBitmap(d.getDropletImage(), d.getX(), d.getY(), null);
                if (!d.updateDroplet()) {
                    dropletIterator.remove();
                }
            }
        }
        dropletLock.unlock();

        fertilizerLock.lock();
        if (!fertilizers.isEmpty()) {
            Iterator<Fertilizer> fertilizerIterator = fertilizers.iterator();
            while (fertilizerIterator.hasNext()) {
                Fertilizer f = fertilizerIterator.next();
                f.draw(canvas);
                if (!f.update(screenHeight)) {
                    fertilizerIterator.remove();
                }
            }
        }
        fertilizerLock.unlock();

        // Update and draw clouds
        updateGameState();
        for (Cloud cloud : clouds) {
            cloud.draw(canvas); // Draw each cloud
        }

        plantDraw(canvas);
        drawStatusBar(canvas);
        drawFPS(canvas);
    }
    public void plantDraw(Canvas canvas) {
        if (isPaused){
            return;
        }
        float screenWidth = canvas.getWidth(); // For a custom view, or canvas.getWidth() otherwise
        float screenHeight = canvas.getHeight(); // For a custom view, or canvas.getHeight() otherwise
        int bitmapWidth = plant.getImageWidth();
        int bitmapHeight = plant.getImageHeight();
        float x = (screenWidth - bitmapWidth) / 2;
        float y = screenHeight - bitmapHeight;

        canvas.drawBitmap(plant.getPlantImage(), x, y, null);
    }

    public void drawStatusBar(Canvas canvas) {
        if (isPaused){
            // Define the position for the pause button at the top left corner of the screen
            float playButtonX = 20; // Adjust the X-coordinate as needed
            float playButtonY = 20; // Adjust the Y-coordinate as needed

            // Draw the pause button at the top left corner of the screen
            canvas.drawBitmap(playIcon, playButtonX, playButtonY, null);

            // Calculate the center of the screen
            float centerX = canvas.getWidth() / 2.0f;
            float centerY = canvas.getHeight() / 2.0f;

            // Calculate the position to render the pause banner in the centre
            float pausedBannerX = centerX - (pausedBanner.getWidth() / 2.0f);
            float pausedBannerY = centerY - (pausedBanner.getHeight() / 2.0f);

            // Draw the paused banner at the centre of the screen
            canvas.drawBitmap(pausedBanner, pausedBannerX, pausedBannerY, null);

            return;
        }

        Paint paint = new Paint();
        paint.setColor(Color.BLACK); // Text color
        paint.setTextSize(45); // Text size
        paint.setAntiAlias(true); // Smooth out the text

        // Define icon size and margins
        int iconSize = 50; // Size of the icon in pixels
        int margin = 50; // Margin from the left edge of the screen
        int statusBarMargin = 30; // Margin from the top edge of the screen

        // Additional vertical shift of the status bar
        int verticalShift = 1000;

        // Draw background with semi-transparent white color
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.argb(128, 255, 255, 255)); // Semi-transparent white
        float cornerRadius = 25.0f;
        int statusBarWidth = 320; // Width of the status bar background
        int statusBarHeight = 390; // Height of the status bar background

        // Draw the rounded rectangle background with the vertical shift
        canvas.drawRoundRect(statusBarMargin, statusBarMargin + verticalShift, statusBarWidth, statusBarHeight + statusBarMargin + verticalShift, cornerRadius, cornerRadius, bgPaint);

        // Set up text drawing properties
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float textHeight = fontMetrics.descent - fontMetrics.ascent;
        float verticalTextOffset = (textHeight / 2) - fontMetrics.descent;

        // Calculate the Y position for the text, which is the vertical center of each line
        float baseLineY = statusBarMargin + verticalShift + verticalTextOffset;

        // Margins for the icons and text within the status bar
        int innerMargin = 40; // Increase this value as needed for more left padding
        int marginTopText = 60; // Adjust the top margin for text
        int marginTopIcon = 55; // Adjust the top margin for icon

        // Adjusted y-coordinates for drawing text and icons
        float textY = baseLineY + marginTopText;
        float iconY = baseLineY - (iconSize / 2) - (textHeight / 2) + marginTopIcon;

        // Draw the text and icons with the vertical shift
        canvas.drawText("% " + plant.getGrowth(), statusBarMargin * 2 + 80 + innerMargin, textY, paint);
        canvas.drawBitmap(growthIcon, statusBarMargin + innerMargin, iconY, null);

        // Increment the Y position for the next line
        float lineSpacing = statusBarHeight / 3;
        textY += lineSpacing;
        iconY += lineSpacing;

        // Draw the second line of text and its icon
        canvas.drawText("% " + plant.getSaturation(), statusBarMargin * 2 + 80 + innerMargin, textY, paint);
        canvas.drawBitmap(saturationIcon, statusBarMargin + innerMargin, iconY, null);

        // Increment the Y position for the next line
        textY += lineSpacing;
        iconY += lineSpacing;

        // Draw the third line of text and its icon
        canvas.drawText("% " + plant.getNutrition(), statusBarMargin * 2 + 80 + innerMargin, textY, paint);
        canvas.drawBitmap(nutritionIcon, statusBarMargin + innerMargin, iconY, null);

        // Define the position for the pause button at the top left corner of the screen
        float pauseButtonX = 20; // Adjust the X-coordinate as needed
        float pauseButtonY = 20; // Adjust the Y-coordinate as needed

        // Draw the pause button at the top left corner of the screen
        canvas.drawBitmap(pauseIcon, pauseButtonX, pauseButtonY, null);
    }

    public void drawFPS(Canvas canvas) {

        canvas.drawText("FPS: " + fps, 880, 40, fpsPaint);
    }

    public void updateGameState() {
        if (isPaused){
            return;
        }
        // Update cloud positions
        for (Cloud cloud : clouds) {
            cloud.update();
        }
        // Include other game state updates here if necessary
    }

}
