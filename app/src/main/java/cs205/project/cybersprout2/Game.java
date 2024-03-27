package cs205.project.cybersprout2;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Game {
    private final Predicate<Consumer<Canvas>> useCanvas;
    private final Plant plant;
    Context context;
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
        this.useCanvas = useCanvas;
        plant = new Plant(context);
        this.executorService = Executors.newFixedThreadPool(3);

        // TODO: handles plant growth > NEED TO ENSURE MUTUAL EXCLUSION
        new Thread(new PlantManager(plant), "plantManager").start();
        // TODO: ensure that thread terminate gracefully
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
            System.out.println("Draw was successful");
        }
    }

    // this method does the actual drawing
    public void draw(Canvas canvas) {
        if (canvas == null) {
            return;
        }
        canvas.drawColor(Color.GRAY);
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
