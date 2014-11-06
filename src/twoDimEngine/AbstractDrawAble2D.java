package twoDimEngine;

public abstract class AbstractDrawAble2D implements DrawAble2D {
	protected PaintMethod2D myPainter;
	protected boolean visible;
	protected boolean destroyed;

	public AbstractDrawAble2D() {
		visible = true;
		destroyed = false;
	}

	@Override
	public abstract void draw(PaintMethod2D painter);

	public PaintMethod2D getMyPainter() {
		return myPainter;
	}
	
	public boolean isVisible() {
		return visible;
	}

	public boolean isDestroyed() {
		return destroyed;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void setDestroyed(boolean destroyed) {
		this.destroyed = destroyed;
	}
	
	public void setMyPainter(PaintMethod2D myPainter) {
		this.myPainter = myPainter;
	}
	
	public abstract BoundingBox getBoundingBox();
}
