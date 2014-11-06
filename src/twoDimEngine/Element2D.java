package twoDimEngine;

import java.awt.Color;

import algebra.Vec2;

public abstract class Element2D extends AbstractDrawAble2D {
	protected int numVertices;
	protected Vec2[] vertices;
	protected Color[] colors;

	public Element2D(int numVertices) {
		super();
		this.numVertices = numVertices;
		vertices = new Vec2[numVertices];
		colors = new Color[numVertices];
		for (int i = 0; i < numVertices; i++) {
			vertices[i] = new Vec2();
			colors[i] = Color.black;
		}
	}

	public abstract void draw(PaintMethod2D painter);

	public int getNumVertices() {
		return numVertices;
	}

	public Vec2[] getVertices() {
		return vertices;
	}

	/**
	 * 
	 * @return color of the first vertex
	 */
	public Color getColor() {
		return colors[0];
	}

	public Color getColor(int index) {
		if (index >= 0 && index < numVertices)
			return colors[index];
		return null;
	}
	
	public Color[] getColors() {
		return colors;
	}

	public Vec2 getVertex(int index) {
		if (index >= 0 && index < numVertices)
			return vertices[index];
		return null;
	}

	public void setVertex(Vec2 vertex, int index) {
		if (index >= 0 && index < numVertices) {
			vertices[index] = vertex;
		}
	}

	/**
	 * 
	 * @param color
	 * @param index
	 *            index of vertex
	 */
	public void setColor(Color color, int index) {
		if (index >= 0 && index < numVertices) {
			colors[index] = color;
		}
	}

	public void setColor(Color color) {
		for (int i = 0; i < numVertices; i++) {
			colors[i] = color;
		}
	}
}
