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
        this.time = System.nanoTime();
    }

    /**
     * Gets eleapsed time in seconds
     *
     * @return the eleapsed time
     */
    public double getEleapsedTime() {
        return (System.nanoTime() - time) / 1E9;
    }

    /**
     * Reset time.
     */
    public void resetTime() {
        this.time = System.nanoTime();
    }
}
