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
                    plant.setSaturation(plant.getSaturation() + 1);
                    dropletLock.lock();
                    try {
                        float dropletX = wateringCan.getX()+100;
                        float dropletY = wateringCan.getY()+425;
                        droplets.add(new Droplet(context, dropletX, dropletY));
                        droplets.add(new Droplet(context, dropletX, dropletY));
                        droplets.add(new Droplet(context, dropletX, dropletY));
                    } finally {
                        dropletLock.unlock();
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

        plantDraw(canvas);


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



    // Call this method when you want to perform background tasks such as loading resources,
    // without blocking the UI thread.
    public void performBackgroundTask(Runnable task) {
        executorService.execute(task);
    }

    // Call this method when the game is closing or you no longer need the ExecutorService
    public void shutdown() {
        executorService.shutdownNow();
    }

}
