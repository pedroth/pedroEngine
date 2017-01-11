package table.src;

import numeric.src.MyMath;
import utils.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DenseNDArray<T> {
    /*
     * row major array
     */
    List<T> denseNDArray;

    int[] powers;

    int[] dim;

    public DenseNDArray(int[] dim) {
        this.dim = dim;
        this.powers = new int[dim.length + 1];
        int acc = 1;
        this.powers[0] = acc;
        for (int i = 0; i < dim.length; i++) {
            acc *= dim[i];
            this.powers[i + 1] = acc;
        }
        denseNDArray = new ArrayList<>(this.powers[this.powers.length - 1]);
        for (int i = 0; i < size(); i++) {
            denseNDArray.add(null);
        }
    }

    public T get(int[] x) {
        checkIndexDimension(x.length);
        return denseNDArray.get(getIndex(x));
    }

    public void set(int[] x, T value) {
        checkIndexDimension(x.length);
        denseNDArray.set(getIndex(x), value);
    }

    public void forEach(Function<T, T> function) {
        int size = size();
        for (int i = 0; i < size; i++) {
            denseNDArray.set(i, function.apply(denseNDArray.get(i)));
        }
    }

    public DenseNDArray<T> get(String x) {
        Interval<Integer>[] intervals = getIntervalFromStr(x);
        int[] newDim = computeNewDim(intervals);
        DenseNDArray<T> newDenseNDArray = new DenseNDArray<>(newDim);
        int size = newDenseNDArray.size();
        int[] y = new int[dim.length];
        for (int i = 0; i < size; i++) {
            int k = 0;
            for (int j = 0; j < intervals.length; j++) {
                Interval<Integer> interval = intervals[j];
                int dx = interval.getXmax() - interval.getXmin();
                int index = i % newDenseNDArray.powers[k + 1] / newDenseNDArray.powers[k];
                k = dx == 0 ? k : k + 1;
                y[j] = dx == 0 ? interval.getXmin() : interval.getXmin() + index;
            }
            newDenseNDArray.denseNDArray.set(i, this.get(y));
        }
        return newDenseNDArray;
    }

    public void set(String x, DenseNDArray<T> vol) {
        Interval<Integer>[] intervals = getIntervalFromStr(x);
        int size = vol.size();
        int[] y = new int[dim.length];
        for (int i = 0; i < size; i++) {
            int k = 0;
            for (int j = 0; j < intervals.length; j++) {
                Interval<Integer> interval = intervals[j];
                int dx = interval.getXmax() - interval.getXmin();
                int index = i % vol.powers[k + 1] / vol.powers[k];
                k = dx == 0 ? k : k + 1;
                y[j] = dx == 0 ? interval.getXmin() : interval.getXmin() + index;
            }
            this.set(y, vol.denseNDArray.get(i));
        }
    }

    private int[] computeNewDim(Interval<Integer>[] intervals) {
        List<Integer> dimBuff = new ArrayList<>(intervals.length);
        for (int i = 0; i < intervals.length; i++) {
            int dx = intervals[i].getXmax() - intervals[i].getXmin();
            if (dx != 0) {
                dimBuff.add(dx + 1);
            }
        }
        int[] newDim = new int[dimBuff.size()];
        for (int i = 0; i < newDim.length; i++) {
            newDim[i] = dimBuff.get(i);
        }
        return newDim;
    }

    private Interval<Integer>[] getIntervalFromStr(String x) {
        String[] split = x.split(",");
        checkIndexDimension(split.length);
        Interval<Integer>[] intervals = new Interval[split.length];
        for (int i = 0; i < split.length; i++) {
            String[] intervalBounds = split[i].split(":");
            switch (intervalBounds.length) {
                case 0:
                    intervals[i] = new Interval<>(0, this.dim[i] - 1);
                    break;
                case 1:
                    Integer integer = Integer.valueOf(intervalBounds[0]);
                    intervals[i] = new Interval<>(integer, integer);
                    break;
                case 2:
                    int xmin = (int) MyMath.clamp(Integer.valueOf(intervalBounds[0]), 0, dim[i] - 1);
                    int xmax = (int) MyMath.clamp(Integer.valueOf(intervalBounds[1]), 0, dim[i] - 1);
                    Interval<Integer> myInterval = new Interval<>(xmin, xmax);
                    if (myInterval.isEmptyInterval()) {
                        throw new RuntimeException("empty interval xmax : " + xmax + " < xmin : " + xmin);
                    }
                    intervals[i] = myInterval;
                    break;
                default:
            }
        }
        return intervals;
    }

    public int size() {
        return this.powers[this.powers.length - 1];
    }

    public int[] getDim() {
        return this.dim;
    }

    private int getIndex(int[] x) {
        int index = 0;
        for (int i = 0; i < dim.length; i++) {
            index += x[i] * powers[i];
        }
        return index;
    }

    private void checkIndexDimension(int d) {
        if (d != dim.length) {
            throw new RuntimeException("index dimension incorrect : " + d + " correct dimension should be : " + dim.length);
        }
    }
}
