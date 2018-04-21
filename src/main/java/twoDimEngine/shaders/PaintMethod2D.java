package twoDimEngine.shaders;

import twoDimEngine.AbstractEngine2D;
import twoDimEngine.elements.Line2D;
import twoDimEngine.elements.Point2D;
import twoDimEngine.elements.Quad2D;
import twoDimEngine.elements.String2D;
import twoDimEngine.elements.Triangle2D;

public abstract class  PaintMethod2D {
	protected AbstractEngine2D engine;
	
	public PaintMethod2D(AbstractEngine2D abstractEngine2D) {
		super();
		this.engine = abstractEngine2D;
	}
	
	public AbstractEngine2D getEngine() {
		return engine;
	}
	public void setEngine(AbstractEngine2D engine) {
		this.engine = engine;
	}
	public abstract void paintTriangle(Triangle2D element);
	public abstract void paintLine(Line2D element);
	public abstract void paintPoint(Point2D element);
	public abstract void paintString(String2D element);
	public abstract void paintQuad(Quad2D element);
}
