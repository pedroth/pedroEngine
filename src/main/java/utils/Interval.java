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

    public X getXmax() {
        return xmax;
    }

    public Interval<X> union(Interval<X> interval) {
        X newXmin = min(this.xmin, interval.getXmin());
        X newXmax = max(this.xmax, interval.getXmax());
        return new Interval<>(newXmin, newXmax);
    }

    private X max(X x, X y) {
        return x.compareTo(y) >= 0 ? x : y;
    }

    private X min(X x, X y) {
        return x.compareTo(y) <= 0 ? x : y;
    }

    public Interval<X> intersection(Interval<X> interval) {
        X newXmin = max(this.xmin, interval.getXmin());
        X newXmax = min(this.xmax, interval.getXmax());
        return new Interval<>(newXmin, newXmax);
    }

    public boolean isEmptyInterval() {
        return isEmptyInterval;
    }

    public void setEmptyInterval(boolean emptyInterval) {
        isEmptyInterval = emptyInterval;
    }

    public boolean isOneItem() {
        return xmax.compareTo(xmin) == 0;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (isEmptyInterval) {
            return "Empty interval";
        }
        stringBuilder.append("[ ").append(xmin.toString()).append(" , ").append(xmax.toString()).append(" ]");
        return stringBuilder.toString();
    }
}
