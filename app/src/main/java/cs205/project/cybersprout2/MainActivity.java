package cs205.project.cybersprout2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  get rid of bar
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        MediaPlayerManager.playBackgroundMusic(MainActivity.this, R.raw.lol);
        setContentView(R.layout.activity_main);
    }

    public void buttonClicked(View view) {
        Intent indent = new Intent(this, GameActivity.class);
        startActivity(indent);
    }
}
