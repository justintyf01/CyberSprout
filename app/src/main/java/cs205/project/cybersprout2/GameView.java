package cs205.project.cybersprout2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.os.Handler;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;

import java.util.function.Consumer;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private final Game game;
    private GameThread gameThread;

    final AtomicBoolean isRightSideTouchActive = new AtomicBoolean(false);

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
        game = new Game(getContext(), this::useCanvas);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameThread != null) {
            int action = event.getActionMasked();
            float x = event.getX();
            float y = event.getY();

            // Determine if the touch event is on the left or right half of the screen
            boolean isRightSide = x > getWidth() / 2.0f;

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    // Check if the pause button is touched
                    if (x > 20 && x < 100 && y > 20 && y < 100) {
                        if (gameThread.isPaused()) {
                            gameThread.resumeGame();
                        } else {
                            gameThread.pauseGame();
                        }
                        return true;
                    }

                    if (isRightSide && !isRightSideTouchActive.get()) {
                        // No active touch on the right side yet, handle this touch
                        game.handleWateringCanTouch(event.getPointerId(0), x, y, true);
                        isRightSideTouchActive.set(true);
                    } else {
                        game.handleFertiliserTouch(event.getPointerId(0), x, y, true);
                        // Handle left side touch if needed, assuming no concurrent right-side touch
                        // needs to be ignored
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    // Optionally handle movement if necessary
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
        }
        // Add a return statement if gameThread is null
        return super.onTouchEvent(event);
    }

    private boolean useCanvas(final Consumer<Canvas> onDraw) {
        boolean result = false;
        final Canvas canvas = getHolder().lockCanvas();
        if (canvas != null) {
            try {
                onDraw.accept(canvas);
            } finally {
                getHolder().unlockCanvasAndPost(canvas);
                result = true;
            }
        }
        return result;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if (gameThread == null || gameThread.getState() == Thread.State.TERMINATED) {
            gameThread = new GameThread(game);
            gameThread.startGame();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        // Handle changes to the surface (e.g., size changes)
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        if (gameThread != null) {
            gameThread.stopGame();
            boolean retry = true;
            while (retry) {
                try {
                    gameThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    // Retry stopping the thread
                }
            }
        }
    }

    public GameThread getGameThread() {
        return gameThread;
    }
}
