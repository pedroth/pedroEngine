package twoDimEngine.elements;

import algebra.Vec2;
import twoDimEngine.BoundingBox;
import twoDimEngine.shaders.PaintMethod2D;

public class Triangle2D extends Element2D {

	public Triangle2D(Vec2 v1, Vec2 v2, Vec2 v3) {
		super(3);
		for (int i = 0; i < 3; i++) {
			switch(i){
			case 0:
				vertices[0] = v1;
				break;
			case 1:
				vertices[1] = v2;
				break;
			case 2:
				vertices[2] = v3;
				break;
			}
		}
	}

	@Override
	public void draw(PaintMethod2D painter) {
		painter.paintTriangle(this);
	}

	@Override
	public BoundingBox getBoundingBox() {
		double xmin = Double.MAX_VALUE;
		double xmax = Double.NEGATIVE_INFINITY;
		double ymin = xmin;
		double ymax = xmax;
		
		for (int i = 0; i < vertices.length; i++) {
			xmin = Math.min(xmin,vertices[i].getX());
			xmax = Math.max(xmax, vertices[i].getX());
			ymin = Math.min(ymin,vertices[i].getY());
			ymax = Math.max(ymax,vertices[i].getY());
		}
		
		return new BoundingBox(xmin, ymin, xmax, ymax);
	}
}
