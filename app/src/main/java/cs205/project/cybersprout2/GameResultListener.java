package cs205.project.cybersprout2;

public interface GameResultListener {
    void onGameOver();
    void onWin();
    void currentStage(int stage);
}