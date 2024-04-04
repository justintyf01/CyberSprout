package cs205.project.cybersprout2;

import android.content.Context;
import android.media.MediaPlayer;
public class MediaPlayerManager {

    private static MediaPlayer mediaPlayer;

    public static void playBackgroundMusic(Context context, int resourceId) {
        // Release the previous MediaPlayer if it exists
        releaseMediaPlayer();

        // Create a new instance of MediaPlayer with the new audio file
        mediaPlayer = MediaPlayer.create(context, resourceId);
        mediaPlayer.setLooping(true); // Set looping to true for background music
        mediaPlayer.start();
    }

    public static void stopBackgroundMusic() {
        // Release the MediaPlayer
        releaseMediaPlayer();
    }

    private static void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}