package cs205.project.cybersprout2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.function.Consumer;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private final Game game = new Game(getContext(), this::useCanvas);
    private GameThread gameThread;

    private final SurfaceHolder surfaceHolder;

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

        setOnTouchListener((view, event) -> {
            int action = event.getActionMasked();
            int index = event.getActionIndex();
            int pointerId = event.getPointerId(index);
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    // Finger went down, tell the Game to add/update a touch point
                    game.handleTouch(pointerId, event.getX(index), event.getY(index), true);
                    break;
                case MotionEvent.ACTION_MOVE:
                    // Handle movement for all active touch points
                    for (int i = 0; i < event.getPointerCount(); i++) {
                        pointerId = event.getPointerId(i);
                        game.handleTouch(pointerId, event.getX(i), event.getY(i), true);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    // Finger lifted, tell the Game to remove the touch point
                    game.handleTouch(pointerId, event.getX(index), event.getY(index), false);
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
}
