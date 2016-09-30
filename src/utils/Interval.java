package utils;

public class Interval<X extends Comparable<X>> {
    private X xmin, xmax;
    private boolean isEmptyInterval;

    public Interval(X xmin, X xmax) {
        if (xmax.compareTo(xmin) >= 0) {
            this.xmin = xmin;
            this.xmax = xmax;
            this.isEmptyInterval = false;
        } else {
            this.isEmptyInterval = true;
        }
    }

    public X getXmin() {
        return xmin;
    }

    public void setXmin(X xmin) {
        this.xmin = xmin;
    }

    public X getXmax() {
        return xmax;
    }

    public void setXmax(X xmax) {
        this.xmax = xmax;
    }

    public boolean isEmptyInterval() {
        return isEmptyInterval;
    }

    public void setEmptyInterval(boolean emptyInterval) {
        isEmptyInterval = emptyInterval;
    }
}
