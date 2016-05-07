package utils;

import java.time.LocalTime;

/**
 * Created by Pedroth on 3/5/2016.
 */
public class TimeInterval {
    private LocalTime tmin, tmax;
    private boolean isEmptyInterval;

    public TimeInterval(LocalTime tmin, LocalTime tmax) {
        if (tmax.compareTo(tmin) > 0) {
            this.tmin = tmin;
            this.tmax = tmax;
            this.isEmptyInterval = false;
        } else {
            this.isEmptyInterval = true;
        }
    }

    public LocalTime getTmin() {
        return tmin;
    }

    public void setTmin(LocalTime tmin) {
        this.tmin = tmin;
    }

    public LocalTime getTmax() {
        return tmax;
    }

    public void setTmax(LocalTime tmax) {
        this.tmax = tmax;
    }

    public boolean isEmptyInterval() {
        return isEmptyInterval;
    }

    public void setEmptyInterval(boolean emptyInterval) {
        isEmptyInterval = emptyInterval;
    }
}
