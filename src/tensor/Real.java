package tensor;

public class Real implements AlgebraField<Double> {
    double x;

    public Real(double x) {
        this.x = x;
    }

    @Override
    public Double sum(Double y) {
        return x + y;
    }

    @Override
    public Double diff(Double y) {
        return x - y;
    }

    @Override
    public Double prod(Double y) {
        return x * y;
    }

    @Override
    public Double div(Double y) {
        return x / y;
    }

    @Override
    public Double sumIdentity() {
        return 0.0;
    }

    @Override
    public Double prodIdentity() {
        return 1.0;
    }

    @Override
    public Double symmetric() {
        return -x;
    }

    @Override
    public Double reciprocal() {
        return 1.0 / x;
    }

    @Override
    public Double clone() {
        return x;
    }
}
