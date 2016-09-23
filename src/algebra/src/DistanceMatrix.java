package algebra.src;


import algebra.utils.AlgebraException;

public class DistanceMatrix extends Matrix {

    //the size of a is n(n-1) / 2
    private double[] a;
    //accumulated indexes
    private int[] accIndex;
    private int n;


    public DistanceMatrix(int n) {
        super();
        this.rows = n;
        this.columns = n;
        this.n = n;
        //it can be showed that n (n - 1) / 2 is always a integer
        this.a = new double[n * (n - 1) / 2];
        this.accIndex = new int[n - 1];
        for (int i = 0; i < accIndex.length; i++) {
            accIndex[i] = accIndexFunction(i);
        }
    }

    public DistanceMatrix(DistanceMatrix distanceMatrix) {
        this(distanceMatrix.getRows());
        for (int i = 2; i <= n; i++) {
            for (int j = 1; j < i; j++) {
                this.setXY(i, j, distanceMatrix.getXY(i, j));
            }
        }
    }

    private int accIndexFunction(int x) {
        //it can be showed that this is always an integer
        // it represents \sum_{k = 1}^{x}{n - x}
        return x * (2 * n - x - 1) / 2;
    }

    @Override
    public double getXY(int x, int y) {
        if (!checkInputIndex(x, y)) {
            throw new AlgebraException("index out of matrix. (x,y) : ( " + x + " , " + y + " )");
        }
        int diff = x - y;
        if (diff == 0) {
            return 0;
        } else if (diff > 0) {
            return a[getIndex(x, y)];
        } else {
            return a[getIndex(y, x)];
        }
    }

    @Override
    public void setXY(int x, int y, double value) {
        if (!checkInputIndex(x, y)) {
            throw new AlgebraException("index out of matrix. (x,y) : ( " + x + " , " + y + " )");
        }
        int diff = x - y;

        if (diff == 0) {
            return;
        }

        if (diff > 0) {
            a[getIndex(x, y)] = value;
        } else {
            a[getIndex(y, x)] = value;
        }
    }

    private int getIndex(int x, int y) {
        return accIndex[y - 1] + (x - y - 1);
    }


    private boolean checkInputIndex(int x, int y) {
        return x <= this.n && x > 0 && y <= n && y > 0;
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
