package twoDimEngine.elements;

import twoDimEngine.BoundingBox;
import twoDimEngine.shaders.PaintMethod2D;
import algebra.Vec2;

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
		return new BoundingBox(Math.min(vertices[0].getX(), vertices[1].getX()), Math.min(vertices[0].getY(), vertices[1].getY()), Math.max(vertices[0].getX(), vertices[1].getX()), Math.max(vertices[0].getY(), vertices[1].getY()));
	}

}
