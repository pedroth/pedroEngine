package twoDimEngine.shaders;

import algebra.src.Vec2;
import twoDimEngine.AbstractEngine2D;
import twoDimEngine.elements.Line2D;

import java.awt.*;

public class ThickLineShader extends FillShader {
    private int thickness = 3;

    public ThickLineShader(AbstractEngine2D abstractEngine2D) {
        super(abstractEngine2D);
    }

    @Override
    public void paintLine(Line2D element) {
        Vec2[] points = element.getVertices();
        for (int i = 0; i < points.length - 1; i++) {
            drawLine(points[i].getX(), points[i].getY(), points[i + 1].getX(),
                    points[i + 1].getY(), element.getColor(i));
        }
    }

    private void drawLine(double x1, double y1, double x2, double y2, Color c) {
        Graphics g = engine.getImageGraphics();
        int i1, j1, i2, j2;
        i1 = (int) engine.IntegerCoordX(x1);
        j1 = (int) engine.IntegerCoordY(y1);
        i2 = (int) engine.IntegerCoordX(x2);
        j2 = (int) engine.IntegerCoordY(y2);
        g.setColor(c);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(3));
        g2.draw(new java.awt.geom.Line2D.Double(i1, j1, i2, j2));
    }

    public int getThickness() {
        return thickness;
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
    }
}
