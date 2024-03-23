package cs205.project.cybersprout2;

public class ElapsedTimer {
    private long startTime = 0L;

    private boolean initialized = false;

    public long getStartTime() {
        return startTime;
    }

    // returns the elapsed time
    public long progress() {
        final long now = System.currentTimeMillis();
        if (!initialized) {
            initialized = true;
            startTime = now;
        }
        final long delta = now - startTime;
//        updateStartTime = now;
        return delta;
    }
}
