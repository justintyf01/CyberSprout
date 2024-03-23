package cs205.project.cybersprout2;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class Game {

    // use this to send notifications -> send in notification method from GameView
//    private final Runnable runnable;

    // this Canvas is a method from GameView, since GameView extends SurfaceHolder
    // this variable allows use to
    private final Predicate<Consumer<Canvas>> useCanvas;
    private final Plant plant;
    private int stage;


    public Game(Context context, final Predicate<Consumer<Canvas>> useCanvas) {
        // add this to the parameter if implementing notifications
//        this.runnable = runnable;
        this.useCanvas = useCanvas;
        plant = new Plant(context);
        stage = 1;
    }

    // Handle clicks
    public void click(MotionEvent event) {
        // TODO: implement logic to spawn watering cans, might require separate thread for this
        for (int i = 0 ; i < event.getPointerCount() ; i++) {
            System.out.println("Show watering cans");
        }
    }

    public void draw() {
        if (useCanvas.test(this::draw)) {
            // can handle other types of metrics here
            System.out.println("Draw was successful");
        }
    }

    // this method does the actual drawing
    public void draw(Canvas canvas) {
        if (canvas == null) {
            return;
        }
//
//        canvas.drawColor(Color.blue(500));
//
//        Paint paint = new Paint();
//        paint.setColor(Color.RED);
//        paint.setStyle(Paint.Style.FILL);
//        paint.setTextSize(50);
//        canvas.drawText("HeLOOOOOO", 150, 500, paint);


        Bitmap[] plantImages = plant.getPlantImages();

        // TODO: Add more plant stages
        Bitmap youngPlant = plantImages[0];
        // Get screen dimensions
        int screenWidth = canvas.getWidth(); // For a custom view, or canvas.getWidth() otherwise
        int screenHeight = canvas.getHeight(); // For a custom view, or canvas.getHeight() otherwise


        if (stage == 1) {
            int bitmapWidth = youngPlant.getWidth();
            int bitmapHeight = youngPlant.getHeight();

            // Calculate X and Y for positioning
            int x = (screenWidth - bitmapWidth) / 2;
            int y = screenHeight - bitmapHeight;
            canvas.drawBitmap(plantImages[0], x, y, null);

        } else if (stage == 2) {
            canvas.drawColor(Color.BLACK);
            Bitmap oldPlant = plantImages[0];

            int bitmapWidth = oldPlant.getWidth();
            int bitmapHeight = oldPlant.getHeight();

            // Calculate X and Y for positioning
            int x = (screenWidth - bitmapWidth) / 2;
            int y = screenHeight - bitmapHeight;
            canvas.drawBitmap(plantImages[0], x, y, null);
        }

    }

}
