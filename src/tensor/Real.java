package tensor;

public class Real implements AlgebraField<Real> {
    protected final double x;

    public Real(double x) {
        this.x = x;
    }

    public double getValue() {
        return this.x;
    }

    @Override
    public Real sum(Real y) {
        return new Real(x + y.x);
    }

    @Override
    public Real diff(Real y) {
        return new Real(x - y.x);
    }

    @Override
    public Real prod(Real y) {
        return new Real(x * y.x);
    }

    @Override
    public Real div(Real y) {
        return new Real(x / y.x);
    }

    @Override
    public Real sumIdentity() {
        return new Real(0.0);
    }

    @Override
    public Real prodIdentity() {
        return new Real(1.0);
    }

    @Override
    public Real symmetric() {
        return new Real(-x);
    }

    @Override
    public Real reciprocal() {
        return new Real(1.0 / this.x);
    }

    @Override
    public Real copy() {
        return new Real(this.x);
    }

    @Override
    public String toString() {
        return "" + x;
    }
}
