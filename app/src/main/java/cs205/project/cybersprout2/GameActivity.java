package cs205.project.cybersprout2;


import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

// The game activity
public class GameActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new GameView(this));
    }
}
