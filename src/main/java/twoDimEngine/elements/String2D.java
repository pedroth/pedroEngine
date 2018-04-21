package twoDimEngine.elements;

import algebra.Vec2;
import twoDimEngine.BoundingBox;
import twoDimEngine.shaders.PaintMethod2D;

import java.awt.*;

public class String2D extends Element2D {
	String string;
	Font font;
	int fontSize;

	public String2D(Vec2 v1, String s) {
		super(1);
		vertices[0] = v1;
		string = s;
		font = new Font("Arial", Font.PLAIN, 12);
	}
	
	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	@Override
	public void draw(PaintMethod2D painter) {
		painter.paintString(this);
	}

	@Override
	public BoundingBox getBoundingBox() {
		return new BoundingBox(vertices[0].getX() - Double.MIN_VALUE, vertices[0].getY() - Double.MIN_VALUE, vertices[0].getX() + Double.MIN_VALUE, vertices[0].getY() + Double.MIN_VALUE);
	}

}
