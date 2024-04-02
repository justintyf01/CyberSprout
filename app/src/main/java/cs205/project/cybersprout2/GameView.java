package cs205.project.cybersprout2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.os.Handler;

import androidx.annotation.NonNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private final Game game = new Game(getContext(), this::useCanvas);
    private GameThread gameThread;
    // Runnable task for updating and drawing clouds
    private Runnable updateCloudsTask = new Runnable() {
        @Override
        public void run() {
            if (game != null) {
                game.updateGameState(); // Ask the game to update its state
                invalidate(); // Trigger onDraw to redraw the view
                handler.postDelayed(this, FRAME_RATE); // Schedule the next update
            }
        }
    };
    private final SurfaceHolder surfaceHolder;
    final AtomicBoolean isRightSideTouchActive = new AtomicBoolean(false);

    private final int FRAME_RATE = 1000 / 60; // For 60 FPS
    private Handler handler = new Handler();

//    private final Bitmap[] plantBitmap;

    @SuppressLint("ClickableViewAccessibility")
    public GameView(Context context) {
        super(context);
        setKeepScreenOn(true);
        surfaceHolder = getHolder();

        // Adds GameView to listen for surfaceCreated/Changed/Destroyed
        surfaceHolder.addCallback(this);

        // set this view in focus
        setFocusable(View.FOCUSABLE);

        // Initialize your clouds and other game elements here
        handler.post(updateCloudsTask); // Start the update task

        // This handles left (Fertilizer) and right (Watering can) touches
        setOnTouchListener((view, event) -> {
            int action = event.getActionMasked();
            float x = event.getX();
            float y = event.getY();

            // Determine if the touch event is on the left or right half of the screen
            boolean isRightSide = x > getWidth() / 2;

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if (isRightSide && !isRightSideTouchActive.get()) {
                        // No active touch on the right side yet, handle this touch
                        game.handleWateringCanTouch(event.getPointerId(0), x, y, true);
                        isRightSideTouchActive.set(true);
                    }  else {
                        game.handleFertiliserTouch(event.getPointerId(0), x, y, true);
                    }

                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isRightSideTouchActive.get()) {
                        // Assuming you want to track movement only for the right side active touch
                        game.handleWateringCanTouch(event.getPointerId(0), x, y, true);
                    } else {
                        game.handleFertiliserTouch(event.getPointerId(0), x, y, true);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: // Consider ACTION_CANCEL to handle interruptions
                    if (isRightSideTouchActive.get()) {
                        // End the right side touch
                        game.handleWateringCanTouch(event.getPointerId(0), x, y, false);
                        isRightSideTouchActive.set(false);
                    } else {
                        game.handleFertiliserTouch(event.getPointerId(0), x, y, false);
                    }
                    break;
            }
            return true;
        });

    }

    // this method returns true to the Game class if the canvas it sent over was successfully drawn
    private boolean useCanvas(final Consumer<Canvas> onDraw) {
        boolean result = false;
        try {
            final SurfaceHolder holder = getHolder();
            final Canvas canvas = holder.lockCanvas();
            try {
                onDraw.accept(canvas);
            } finally {
                try {
                    holder.unlockCanvasAndPost(canvas);
                    result = true;
                } catch (final IllegalStateException e) {
                    // Do nothing
                }
            }
        } catch (final IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if ((gameThread == null) || (gameThread.getState() == Thread.State.TERMINATED)) {
            // Create the game thread
            gameThread = new GameThread(game);
        }
        // Start the thread
        gameThread.startGame();

        // TODO: remove, used for initial testing
//        Canvas canvas = surfaceHolder.lockCanvas();
//        myDraw(canvas);
//        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        // stop the thread
//        gameThread.stopGame();
//        try {
//            // ensure it terminates gracefully
//            gameThread.join();
//        } catch (InterruptedException e) {
//            System.out.println(e.getMessage());
//        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        game.draw();
    }

//    public void myDraw(Canvas canvas) {
//        canvas.drawColor(Color.WHITE);
//
//        Paint paint = new Paint();
//        paint.setColor(Color.YELLOW);
//        paint.setStyle(Paint.Style.FILL);
//        paint.setTextSize(50);
//        canvas.drawText("HeLOOOOOO", 150, 150, paint);
//    }

}
