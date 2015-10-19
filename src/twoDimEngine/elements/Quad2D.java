package twoDimEngine.elements;

import twoDimEngine.BoundingBox;
import twoDimEngine.shaders.PaintMethod2D;
import algebra.Vec2;

public class Quad2D extends Element2D {
	
	
	
	public Quad2D(Vec2 v1, Vec2 v2, Vec2 v3, Vec2 v4) {
		super(4);		
		for (int i = 0; i < numVertices; i++) {
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
			case 3:
				vertices[3] = v4;
				break;
			}
		}
	}

	@Override
	public void draw(PaintMethod2D painter) {
		painter.paintQuad(this);
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
