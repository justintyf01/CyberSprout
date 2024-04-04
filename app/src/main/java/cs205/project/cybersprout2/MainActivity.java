package cs205.project.cybersprout2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MediaPlayerManager.playBackgroundMusic(MainActivity.this, R.raw.lol);
        setContentView(R.layout.activity_main);
    }

    public void buttonClicked(View view) {
        Intent indent = new Intent(this, GameActivity.class);
        MediaPlayerManager.playBackgroundMusic(MainActivity.this, R.raw.lol3);
        startActivity(indent);
    }
}
