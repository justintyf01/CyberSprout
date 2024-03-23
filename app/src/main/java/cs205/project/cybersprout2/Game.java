package cs205.project.cybersprout2;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Game {

    // use this to send notifications -> send in notification method from GameView
    // private final Runnable runnable;

    // this Canvas is a method from GameView, since GameView extends SurfaceHolder
    // this variable allows use to
    private final Predicate<Consumer<Canvas>> useCanvas;
    private final Plant plant;
    private int stage = 0;
    private final ElapsedTimer elapsedTimer = new ElapsedTimer();
    private long totalElapsedTime = 0;
//    private List<WateringCan> wateringCanList = new ArrayList<>();
    Context context;
    private Map<Integer, WateringCan> activeTouches = new HashMap<>();
    private ExecutorService executorService;

    public Game(Context context, final Predicate<Consumer<Canvas>> useCanvas) {
        // add this to the parameter if implementing notifications
//        this.runnable = runnable;
        this.context = context;
        this.useCanvas = useCanvas;
        plant = new Plant(context);
    }

    public void handleTouch(int pointerId, float x, float y, boolean isDown) {
        if (isDown) {
//            activeTouches.put(pointerId, new PointF(x, y));
            activeTouches.put(pointerId, new WateringCan(context, x, y));
        } else {
            // If the finger is lifted, remove the touch point
            activeTouches.remove(pointerId);
        }
    }
    public void draw() {
        if (useCanvas.test(this::draw)) {
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

    public void waterPlant() {
        // Set state to watering
        // Start animation for the water effect
        // Update plant's growth if necessary
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
