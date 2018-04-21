package graphicEngine;

public class Simplex extends AbstractSimplex {
    private PaintMethod<Simplex> painter;

    public Simplex(int numOfVertices) {
        super(numOfVertices);
    }

    public PaintMethod<Simplex> getPainter() {
        return painter;
    }

    public void setPainter(PaintMethod<Simplex> painter) {
        this.painter = painter;
    }

    @Override
    public void draw() {
        painter.paint(this);
    }
}
