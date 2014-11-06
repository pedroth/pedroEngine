package twoDimEngine;

import java.awt.Font;

import algebra.Vec2;

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

	public Font getFont() {
		return font;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setString(String string) {
		this.string = string;
	}

	public void setFont(Font font) {
		this.font = font;
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
		return new BoundingBox();
	}

}
