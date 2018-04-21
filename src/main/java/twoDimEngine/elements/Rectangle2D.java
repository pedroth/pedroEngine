package twoDimEngine.elements;

import algebra.Matrix;
import algebra.Vec2;
import twoDimEngine.AbstractDrawAble2D;
import twoDimEngine.BoundingBox;
import twoDimEngine.shaders.PaintMethod2D;

import java.awt.*;

public class Rectangle2D extends AbstractDrawAble2D {
	private Vec2 pos;
	private double angle;
	private double width;
	private double height;
	private Quad2D element;
	/**
	 * coordinates of rotation center given in model space, where referential is
	 * [width,0] and [0,height]
	 */
	private Vec2 rotationCenterCoord;

	public Rectangle2D(Vec2 pos, double angle, double width, double height) {
		super();
		this.pos = pos;
		this.angle = angle;
		this.width = width;
		this.height = height;
		this.element = new Quad2D(pos, Vec2.add(pos, new Vec2(width, 0.0)),
				Vec2.add(pos, new Vec2(width, height)), Vec2.add(pos, new Vec2(
						0.0, height)));
		rotationCenterCoord = new Vec2(0.5, 0.5);
		setQuad();
	}

	public Vec2 getPos() {
		return pos;
	}

	public void setPos(Vec2 pos) {
		this.pos = pos;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public Color[] getColors() {
		return element.getColors();
	}

	/**
	 *
	 * @return color of the first vertex
	 */
	public Color getColor() {
		return element.getColor();
	}

	public void setColor(Color color) {
		element.setColor(color);
	}

	public Color getColor(int index) {
		return element.getColor(index);
	}

	/**
	 *
	 * @return coordinates of rotation center given in model space, where
	 *         referential is [width,0] and [0,height]
	 */
	public Vec2 getRotationCenterCoord() {
		return rotationCenterCoord;
	}

	/**
	 *
	 * @param rotationCenterCoord
	 *            coordinates of rotation center given in model space, where
	 *            referential is [width,0] and [0,height]
	 */
	public void setRotationCenterCoord(Vec2 rotationCenterCoord) {
		this.rotationCenterCoord = rotationCenterCoord;
	}

	/**
	 *
	 * @param color
	 * @param index
	 *            index of vertex
	 */
	public void setColor(Color color, int index) {
		element.setColor(color, index);
	}

	public Quad2D getElement() {
		return element;
	}

	@Override
	public void draw(PaintMethod2D painter) {
		setQuad();
		element.draw(painter);
	}

	@Override
	public BoundingBox getBoundingBox() {
		return element.getBoundingBox();
	}

	private void setQuad() {
		Vec2[] vertex = element.getVertices();

		Vec2 modelCenter = new Vec2(rotationCenterCoord.getX() * width,
				rotationCenterCoord.getY() * height);

		double radians = angle;
		double cosA = Math.cos(radians);
		double sinA = Math.sin(radians);
		double[][] m = { { cosA, -sinA }, { sinA, cosA } };

		Matrix rotation = new Matrix(m);

		vertex[0] = Vec2.diff(new Vec2(), modelCenter);
		vertex[1] = Vec2.diff(new Vec2(width, 0.0), modelCenter);
		vertex[2] = Vec2.diff(new Vec2(width, height), modelCenter);
		vertex[3] = Vec2.diff(new Vec2(0.0, height), modelCenter);
		for (int i = 0; i < vertex.length; i++) {
			vertex[i] = Vec2.matrixProd(rotation, vertex[i]);
			vertex[i] = Vec2.add(pos, vertex[i]);
		}
	}
}