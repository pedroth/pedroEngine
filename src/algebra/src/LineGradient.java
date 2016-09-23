package algebra.src;


public class LineGradient extends TridiagonalMatrix {

    public LineGradient(int n) {
        super(n - 1, n);
        for (int i = 1; i <= n - 1; i++) {
            this.setXY(i, i, -1);
            this.setXY(i, i + 1, 1);
        }
    }
}
