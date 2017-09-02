package numeric.src;

import algebra.src.Diagonal;
import algebra.src.Vector;
import realFunction.src.ExpressionFunction;

import java.util.Arrays;

public class MyMath {
    public static double clamp(double x, double xmin, double xmax) {
        return Math.max(xmin, Math.min(xmax, x));
    }

    public static double dirac(double x) {
        if (x == 0)
            return 1.0;
        else
            return 0;
    }

    public static double heaviside(double x) {
        if (x > 0)
            return 1.0;
        else if (x == 0)
            return 0.5;
        else
            return 0;
    }

    public static double step(double x) {
        if (x >= 0)
            return 1.0;
        else
            return 0;
    }

    /**
     * http://www.johndcook.com/blog/cpp_phi/
     *
     * @param x
     * @return gaussian cumulative distribution function
     */
    public static double phi(double x) {
        // constants
        double a1 = 0.254829592;
        double a2 = -0.284496736;
        double a3 = 1.421413741;
        double a4 = -1.453152027;
        double a5 = 1.061405429;
        double p = 0.3275911;

        // Save the sign of x
        int sign = 1;
        if (x < 0)
            sign = -1;
        x = Math.abs(x) / Math.sqrt(2.0);

        // A&S formula 7.1.26
        double t = 1.0 / (1.0 + p * x);
        double y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x);

        return 0.5 * (1.0 + sign * y);
    }

    /**
     * @param x
     * @param myu
     * @param sigma
     * @return normalized gaussian distribution between [0,1]
     */
    public static double kernel(double x, double myu, double sigma) {
        double normalization = MyMath.phi((1 - myu) / sigma) - MyMath.phi(-myu / sigma);
        double gaussian = gaussian(x, myu, sigma);
        return gaussian / normalization;
    }

    /**
     * normal distribution
     *
     * @param x
     * @param myu
     * @param sigma
     * @return
     */
    public static double gaussian(double x, double myu, double sigma) {
        return (1 / (Math.sqrt(2 * Math.PI) * sigma)) * Math.exp(-0.5 * ((x - myu) / sigma) * ((x - myu) / sigma));
    }

    public static double gaussian(Vector x, Vector myu, Diagonal sigma) {
        int dim = x.getDim();
        Vector diff = Vector.diff(x, myu);
        Diagonal inverse = sigma.inverse();
        return (1 / (Math.sqrt(powInt(2 * Math.PI, dim) * sigma.det()))) * Math.exp(-0.5 * Vector.innerProd(diff, inverse.prodVector(diff)));
    }

    /**
     * warning does not accept variables
     *
     * @param s string to be read.
     * @return computation of the expression in s
     */
    public static double numericRead(String s) {
        ExpressionFunction in;
        in = new ExpressionFunction(s, new String[]{});
        in.init();
        return in.compute(new Vector(1));
    }

    public static int positiveMod(int x, int y) {
        if (x < 0) {
            return y + (x % y);
        } else {
            return x % y;
        }
    }

    public static double powInt(double x, int k) {
        if (k == 0) {
            return 1;
        } else if (k == 1) {
            return x;
        } else {
            int q = (int) Math.floor(k / 2);
            int r = k % 2;
            if (r == 0) {
                return powInt(x * x, q);
            } else {
                return x * powInt(x * x, q);
            }
        }
    }

    public static <T> T[] permutate(T[] array, int[] permutation) {
        assert array.length != permutation.length : "input array sizes must be equal";
        final T[] copy = Arrays.copyOf(array, array.length);
        for (int i = 0; i < permutation.length; i++) {
            copy[i] = array[permutation[i]];
        }
        return copy;
    }
}
