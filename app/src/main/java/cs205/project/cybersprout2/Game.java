package cs205.project.cybersprout2;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Paint;

public class Game {
    /************************** SYSTEM **************************/
    private final Context context;
    private final Predicate<Consumer<Canvas>> useCanvas;
    private final int screenWidth;
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

    /****************** WATERING CAN AND DROPLETS ******************/
    private final Map<Integer, WateringCan> activeTouches = new HashMap<>();
    private WateringCan wateringCan;
    private final List<Droplet> droplets = new ArrayList<>();
    private final ReentrantLock dropletLock = new ReentrantLock();
    private final ExecutorService executorService;
    // private BackgroundTaskThreadPool threadPool = BackgroundTaskThreadPool.getThreadPool();

    /****************** STATUS BAR ******************/
    private final Bitmap growthIcon;
    private final Bitmap nutritionIcon;
    private final Bitmap saturationIcon;

    /****************** CLOUDS ******************/

    private final Bitmap cloud1, cloud2, cloud3, cloud4;
    private List<Cloud> clouds = new ArrayList<>();
    private boolean cloudsScaled = false;


    public Game(Context context, final Predicate<Consumer<Canvas>> useCanvas) {
        // add this to the parameter if implementing notifications
//        this.runnable = runnable;
        this.context = context;
        this.screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        this.screenHeight = context.getResources().getDisplayMetrics().heightPixels;

        this.useCanvas = useCanvas;
        plant = new Plant(context);
        this.executorService = Executors.newFixedThreadPool(3);

        this.sun = BitmapFactory.decodeResource(context.getResources(), R.drawable.sun);
        this.moon = BitmapFactory.decodeResource(context.getResources(), R.drawable.moon);

        int logoSize = 80;

        Bitmap originalGrowthIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.growth_icon);
        Bitmap originalSaturationIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.saturation_icon);
        Bitmap originalNutritionIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.nutrition_icon);

        this.growthIcon = Bitmap.createScaledBitmap(originalGrowthIcon, logoSize, logoSize, true);
        this.saturationIcon = Bitmap.createScaledBitmap(originalSaturationIcon, logoSize, logoSize, true);
        this.nutritionIcon = Bitmap.createScaledBitmap(originalNutritionIcon, logoSize, logoSize, true);

        int cloudWidth = 200; // Desired width for the cloud images
        int cloudHeight = 150; // Desired height for the cloud images

        Bitmap cloud1Bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud1);
        this.cloud1 = Bitmap.createScaledBitmap(cloud1Bitmap, cloudWidth, cloudHeight, false);

        Bitmap cloud2Bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud2);
        this.cloud2 = Bitmap.createScaledBitmap(cloud2Bitmap, cloudWidth, cloudHeight, false);

        Bitmap cloud3Bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud3);
        this.cloud3 = Bitmap.createScaledBitmap(cloud3Bitmap, cloudWidth, cloudHeight, false);

        Bitmap cloud4Bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud4);
        this.cloud4 = Bitmap.createScaledBitmap(cloud4Bitmap, cloudWidth, cloudHeight, false);


        clouds.add(new Cloud(context, cloud1, 0, 200, -1.2f)); // Cloud 1
        clouds.add(new Cloud(context, cloud2, screenWidth, 100, 1.1f)); // Cloud 2
        clouds.add(new Cloud(context, cloud3, screenWidth / 2, 300, 0.9f)); // Cloud 3

        new Thread(new PlantManager(plant), "plantManager").start();

        this.background = new Background(context, screenWidth, screenHeight);
        // Start thread to constantly update background values based on real time
        new Thread(new BackgroundUpdater(background), "Background Color Handler").start();

    }

    public void updatePosition(float x, float y, boolean isSun) {
        this.bodyX = x;
        this.bodyY = y;
        this.isSun = isSun;
    }

    public void handleTouch(int pointerId, float x, float y, boolean isDown) {
        if (isDown) {
//            activeTouches.put(pointerId, new PointF(x, y));
            this.wateringCan = new WateringCan(context, x, y);
            activeTouches.put(pointerId, wateringCan);

            executorService.execute(() -> {
                while (activeTouches.get(pointerId) != null) {
                    if (wateringCan != null) { // Add this check
                        plant.setSaturation(plant.getSaturation() + 1);
                        dropletLock.lock();
                        try {
                            float dropletX = wateringCan.getX() + 100; // This line was causing the crash
                            float dropletY = wateringCan.getY() + 425;
                            droplets.add(new Droplet(context, dropletX, dropletY));
                            // Other operations
                        } finally {
                            dropletLock.unlock();
                        }
                    }
                    try {
                        Thread.sleep(50);
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
    public void draw() {
        if (useCanvas.test(this::draw)) {
            // Can implement framerate counter here
//            System.out.println("Draw was successful");
        }
    }

    // this method does the actual drawing
    public void draw(Canvas canvas) {

        if (canvas == null) {
            return;
        }
        canvas.drawBitmap(background.getBg(), 0,0,null);
//        canvas.drawColor(currentColor.get());
//        canvas.drawColor(Color.GRAY);
        for (WateringCan can : activeTouches.values()) {
            canvas.drawBitmap(can.getWateringCanImage(), can.getX(), can.getY(), null);
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

        // Update and draw clouds
        updateGameState();
        for (Cloud cloud : clouds) {
            cloud.draw(canvas); // Draw each cloud
        }

        plantDraw(canvas);
        drawStatusBar(canvas);

//        canvas.drawBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.sun), 0,800
//                ,null);
//        canvas.drawBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.moon), 0,0,null);

    }

    public void plantDraw(Canvas canvas) {
        float screenWidth = canvas.getWidth(); // For a custom view, or canvas.getWidth() otherwise
        float screenHeight = canvas.getHeight(); // For a custom view, or canvas.getHeight() otherwise
        int bitmapWidth = plant.getImageWidth();
        int bitmapHeight = plant.getImageHeight();
        float x = (screenWidth - bitmapWidth) / 2;
        float y = screenHeight - bitmapHeight;

        canvas.drawBitmap(plant.getPlantImage(), x, y, null);
    }

    public void drawStatusBar(Canvas canvas) {
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
    }

    public void updateGameState() {
        // Update cloud positions
        for (Cloud cloud : clouds) {
            cloud.update();
        }
        // Include other game state updates here if necessary
    }

}
