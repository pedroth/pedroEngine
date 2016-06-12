package utils;

/**
 * Created by Pedroth on 6/10/2016.
 */
public class StopWatch {
    private double time;

    /**
     * Instantiates a new Stop watch.
     */
    public StopWatch() {
        this.time = System.currentTimeMillis();
    }

    /**
     * Gets eleapsed time in seconds
     *
     * @return the eleapsed time
     */
    public double getEleapsedTime() {
        return (System.currentTimeMillis() - time) / 1000;
    }

    /**
     * Reset time.
     */
    public void resetTime() {
        this.time = System.currentTimeMillis();
    }
}
