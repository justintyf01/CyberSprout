package cs205.project.cybersprout2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

//        setOnTouchListener((view, event) -> {
//            game.click(event);
//            return true;
//        });

        // Initialise Bitmap
//        plantBitmap = new Bitmap[1];
//        plantBitmap[0] = BitmapFactory.decodeResource(getResources(), R.drawable.plant1);

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
