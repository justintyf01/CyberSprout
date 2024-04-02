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

import androidx.annotation.NonNull;

import java.util.function.Consumer;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private final Game game;
    private GameThread gameThread;
    final Handler handler = new Handler();

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

            if (action == MotionEvent.ACTION_DOWN) {
                // Check if the pause button is touched
                if (x > 20 && x < 100 && y > 20 && y < 100) {
                    if (gameThread.isPaused()) {
                        gameThread.resumeGame();
                    } else {
                        gameThread.pauseGame();
                    }
                    return true;
                }

                // Pass touch events to game for handling
                game.handleTouch(event);
            }
        }
        return true;
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

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        // Draw game elements here, including the pause button
        if (game != null) {
            game.draw();
        }
        drawPauseButton(canvas);
    }

    private void drawPauseButton(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.YELLOW);
        canvas.drawRect(20, 20, 100, 100, paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        canvas.drawText(gameThread != null && gameThread.isPaused() ? "Resume" : "Pause", 25, 75, paint);
    }
}
