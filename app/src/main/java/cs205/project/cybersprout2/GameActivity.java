package cs205.project.cybersprout2;

import android.app.Activity;
import android.os.Bundle;

public class GameActivity extends Activity {
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameView = new GameView(this);
        setContentView(gameView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the game thread
        if (gameView != null && gameView.getGameThread() != null) {
            gameView.getGameThread().pauseGame();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the game thread
        if (gameView != null && gameView.getGameThread() != null) {
            gameView.getGameThread().resumeGame();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the game thread
        if (gameView != null && gameView.getGameThread() != null) {
            gameView.getGameThread().stopGame();
        }
    }
}
