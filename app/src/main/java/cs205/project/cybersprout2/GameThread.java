package cs205.project.cybersprout2;

public class GameThread extends Thread {

    private final Game game;
    private boolean isRunning;

    public GameThread(Game game) {
        this.game = game;
        isRunning = false;
    }

    public void startGame() {
        isRunning = true;
        start();
    }

    @Override
    public void run() {

        while (isRunning) {
            // draw the plant
            game.draw();
//            game.update();
        }
    }

//    public void game_sleep() {
//        try {
//            sleep(1000);
//        } catch (InterruptedException e) {
//            System.out.println(e.getMessage());
//        }
//    }

}
