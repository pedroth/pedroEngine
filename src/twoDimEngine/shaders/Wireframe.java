package twoDimEngine.shaders;

import algebra.src.Vec2;
import twoDimEngine.AbstractEngine2D;
import twoDimEngine.elements.*;

import java.awt.*;

public class Wireframe extends PaintMethod2D {

	public Wireframe(AbstractEngine2D abstractEngine2D) {
		super(abstractEngine2D);
	}

	private void drawLine(double x1, double y1, double x2, double y2,Color c) {
		Graphics g = engine.getImageGraphics();
		int i1, j1, i2, j2;
		i1 = (int) engine.IntegerCoordX(x1);
		j1 = (int) engine.IntegerCoordY(y1);
		i2 = (int) engine.IntegerCoordX(x2);
		j2 = (int) engine.IntegerCoordY(y2);
		g.setColor(c);
		g.drawLine(i1, j1, i2, j2);
	}
	
	private void paintPolygon(Element2D e) {
		Vec2[] points = e.getVertices();
		int length = points.length;
		for (int i = 0; i < length; i++) {
			drawLine(points[i].getX(), points[i].getY(), points[(i + 1)
					% length].getX(), points[(i + 1) % length].getY(),e.getColor(i));
		}
	}

	@Override
	public void paintTriangle(Triangle2D element) {
		paintPolygon(element);
	}

	@Override
	public void paintLine(Line2D element) {
		Vec2[] points = element.getVertices();
		for (int i = 0; i < points.length - 1; i++) {
			drawLine(points[i].getX(), points[i].getY(), points[i + 1].getX(),
					points[i + 1].getY(), element.getColor(i));
		}
	}

	@Override
	public void paintPoint(Point2D element) {
		Graphics g = engine.getImageGraphics();
		int i, j;
		i = (int) engine.IntegerCoordX(element.getVertex(0).getX());
		j = (int) engine.IntegerCoordY(element.getVertex(0).getY());
		g.setColor(element.getColor(0));
		
		double radius = element.getRadius();
		
		if(radius == 0.0)
			g.drawLine(i, j, i, j);
		else {
			int rx = (int) (engine.IntegerCoordX(element.getRadius()) - engine.IntegerCoordX(0.0));
			int ry = (int) Math.abs(engine.IntegerCoordY(element.getRadius()) - engine.IntegerCoordY(0.0));
			g.drawOval(i - rx, j - ry, 2 * rx, 2 * ry);
		}
	}

	@Override
	public void paintString(String2D element) {
		Vec2[] points = element.getVertices();
		double x = points[0].getX();
		double y = points[0].getY();
		int i = (int) engine.IntegerCoordX(x);
		int j = (int) engine.IntegerCoordY(y);
		Graphics g = engine.getImageGraphics();
		g.setFont(element.getFont());
		g.setColor(element.getColor(0));
		g.drawString(element.getString(), i, j);
	}

	@Override
	public void paintQuad(Quad2D element) {
		paintPolygon(element);
	}
}
