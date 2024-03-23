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

    // Handle clicks
//    public void click(MotionEvent event) {
//        // TODO: implement logic to spawn watering cans, might require separate thread for this
//        for (int i = 0 ; i < event.getPointerCount() ; i++) {
//            float wateringCanX = event.getX();
//            float wateringCanY = event.getY();
//
//        }
//    }
    public boolean click(MotionEvent event) {
        int action = event.getActionMasked(); // Get the type of action
        int index = event.getActionIndex(); // Get the index of the pointer associated with the action
        int pointerId = event.getPointerId(index); // Get the ID of the pointer

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: // A non-primary pointer has gone down.
                // Handle touch down (start tracking the touch)
                float x = event.getX(index);
                float y = event.getY(index);

                // Use the pointerId to track this specific touch point
                break;

            case MotionEvent.ACTION_MOVE:
                // Here, you may want to track movement of all active touch points.
                // Loop through all active pointers
                for (int i = 0; i < event.getPointerCount(); i++) {
                    int id = event.getPointerId(i);
                    x = event.getX(i);
                    y = event.getY(i);
                    // Update your view or model based on the movement of this pointer
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP: // A non-primary pointer has gone up.
                // Handle touch end (stop tracking the touch)
                // Use the pointerId to stop tracking this specific touch point
                break;

            case MotionEvent.ACTION_CANCEL:
                // Handle touch cancel (clear tracking of touches)
                break;
        }
        return true; // Indicate we've handled the touch event
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

//        if (stage == 0) {
//            canvas.drawColor(Color.GRAY);
//        }
//
        Bitmap[] plantImages = plant.getPlantImages();
        Bitmap plantImage = plantImages[0];
        int bitmapWidth = plantImage.getWidth();
        int bitmapHeight = plantImage.getHeight();
        // Get screen dimensions
        int screenWidth = canvas.getWidth(); // For a custom view, or canvas.getWidth() otherwise
        int screenHeight = canvas.getHeight(); // For a custom view, or canvas.getHeight() otherwise
        int x = (screenWidth - bitmapWidth) / 2;
        int y = screenHeight - bitmapHeight;

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

    public void update() {
//        totalElapsedTime += elapsedTimer.progress(); // Accumulate total elapsed time
//        int newStage = (int) (totalElapsedTime / 100000) % 18; // Convert milliseconds to seconds
//
//        if (newStage != stage) { // Check if a new second has passed
//            stage = newStage;
//            // Here, you can also do whatever you need to do when stage increases.
//        }
        stage++;
        if (stage == 18) {
            stage = 0;
        }
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
