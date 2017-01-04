package twoDimEngine.shaders;

import algebra.src.Vec2;
import twoDimEngine.AbstractEngine2D;
import twoDimEngine.elements.Element2D;
import twoDimEngine.elements.Point2D;
import twoDimEngine.elements.Quad2D;
import twoDimEngine.elements.Triangle2D;

import java.awt.*;


public class FillShader extends Wireframe {

    public FillShader(AbstractEngine2D abstractEngine2D) {
        super(abstractEngine2D);
    }


    private void paintPoly(Element2D element2d) {
        Graphics g = engine.getImageGraphics();
        Vec2[] vec = element2d.getVertices();
        int length = vec.length;
        int[] x = new int[length];
        int[] y = new int[length];
        for (int i = 0; i < length; i++) {
            x[i] = (int) engine.integerCoordX(vec[i].getX());
            y[i] = (int) engine.integerCoordY(vec[i].getY());
        }
        g.setColor(element2d.getColor(0));
        g.fillPolygon(x, y, length);

    }

    @Override
    public void paintTriangle(Triangle2D element) {
        paintPoly(element);
    }

    @Override
    public void paintQuad(Quad2D element) {
        paintPoly(element);
    }

    @Override
    public void paintPoint(Point2D element) {
        Graphics g = engine.getImageGraphics();
        int i = (int) engine.integerCoordX(element.getVertex(0).getX());
        int j = (int) engine.integerCoordY(element.getVertex(0).getY());
        g.setColor(element.getColor(0));

        double radius = element.getRadius();

        if (radius == 0.0)
            g.drawLine(i, j, i, j);
        else {
            int rx = (int) (engine.integerCoordX(element.getRadius()) - engine.integerCoordX(0.0));
            int ry = (int) Math.abs(engine.integerCoordY(element.getRadius()) - engine.integerCoordY(0.0));

            if (rx == 0 || ry == 0)
                g.drawLine(i, j, i, j);
            else
                g.fillOval(i - rx, j - ry, 2 * rx, 2 * ry);
        }
    }
}
