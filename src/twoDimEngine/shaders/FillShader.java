package twoDimEngine.shaders;

import java.awt.Graphics;

import twoDimEngine.AbstractEngine2D;
import twoDimEngine.elements.Element2D;
import twoDimEngine.elements.Point2D;
import twoDimEngine.elements.Quad2D;
import twoDimEngine.elements.Triangle2D;
import algebra.Vec2;


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
			x[i] = (int) engine.IntegerCoordX(vec[i].getX());
			y[i] = (int) engine.IntegerCoordY(vec[i].getY());
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
		int i = (int) engine.IntegerCoordX(element.getVertex(0).getX());
		int j = (int) engine.IntegerCoordY(element.getVertex(0).getY());
		g.setColor(element.getColor(0));
		
		double radius = element.getRadius();
		
		if(radius == 0.0)
			g.drawLine(i, j, i, j);
		else {
			int rx = (int) (engine.IntegerCoordX(element.getRadius()) - engine.IntegerCoordX(0.0));
			int ry = (int) Math.abs(engine.IntegerCoordY(element.getRadius()) - engine.IntegerCoordY(0.0));
			g.fillOval(i - rx, j - ry, 2 * rx, 2 * ry);
		}
	}
}
