package algebra.utils;

public class MatlabMatrixPrinter implements MatrixPrinter {

    @Override
    public String before() {
        return "[";
    }

    @Override
    public String after() {
        return "]";
    }

    @Override
    public String line() {
        return ";";
    }

    @Override
    public String separator() {
        return ",";
    }
}