package twoDimEngine.elements;

import algebra.Vec2;
import twoDimEngine.BoundingBox;
import twoDimEngine.shaders.PaintMethod2D;

public class Point2D extends Element2D {
	
	private double radius;
	
	public Point2D(Vec2 v1) {
		super(1);
		vertices[0] = v1;
		radius = 0.0;
	}
	
	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		if(radius < 0)
			this.radius = 0.0;
		else
			this.radius = radius;
	}

	public Vec2 getPos() {
		return getVertex(0);
	}
	
	public void setPos(Vec2 pos) {
		vertices[0].setX(pos.getX());
		vertices[0].setY(pos.getY());
	}

	@Override
	public void draw(PaintMethod2D painter) {
		painter.paintPoint(this);
	}

	@Override
	public BoundingBox getBoundingBox() {
		if(radius > 0)
			return new BoundingBox(vertices[0].getX() - radius, vertices[0].getY() - radius, vertices[0].getX() + radius, vertices[0].getY() + radius);
		else
			return new BoundingBox(vertices[0].getX() - Double.MIN_VALUE, vertices[0].getY() - Double.MIN_VALUE, vertices[0].getX() + Double.MIN_VALUE, vertices[0].getY() + Double.MIN_VALUE);
	}

	
}
