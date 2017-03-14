package twoDimEngine.elements;

import algebra.src.Vec2;
import twoDimEngine.BoundingBox;
import twoDimEngine.shaders.PaintMethod2D;

public class Line2D extends Element2D {

	public Line2D(Vec2 v1, Vec2 v2) {
		super(2);
		for (int i = 0; i < 2; i++) {
			switch (i) {
			case 0:
				vertices[0] = v1;
				break;
			case 1:
				vertices[1] = v2;
				break;
			}
		}
	}

	@Override
	public void draw(PaintMethod2D painter) {
		painter.paintLine(this);
	}

	@Override
	public BoundingBox getBoundingBox() {
		double x = vertices[0].getX();
		double x1 = vertices[1].getX();
		double y = vertices[0].getY();
		double y1 = vertices[1].getY();
		return new BoundingBox(Math.min(x, x1), Math.min(y, y1), Math.max(x, x1), Math.max(y, y1));
	}

}
