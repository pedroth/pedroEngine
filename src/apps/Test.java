package apps;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import twoDimEngine.FillShader;
import twoDimEngine.Line2D;
import twoDimEngine.PaintMethod2D;
import twoDimEngine.Point2D;
import twoDimEngine.Quad2D;
import twoDimEngine.Rectangle2D;
import twoDimEngine.TwoDimEngine;
import algebra.Vec2;

public class Test extends MyFrame {
	private TwoDimEngine engine;
	private PaintMethod2D shader;
	private Vec2[] thief;
	private Point2D point;

	public Test(String title, int width, int height) {
		super(title, width, height);
		engine = new TwoDimEngine(width, height);
		shader = new FillShader(engine);
		engine.setBackGroundColor(Color.white);
		engine.setCamera(-10, 10, -10, 10);
		thief = new Vec2[4];
		thief[0] = new Vec2(0, 0);
		thief[1] = new Vec2(1, 0);
		thief[2] = new Vec2(1, 1);
		thief[3] = new Vec2(0, 1);
		point = new Point2D(new Vec2(5.0, 0.0));
		point.setColor(Color.red);
		point.setRadius(1);
		engine.addtoList(point, shader);
		Quad2D quad = new Quad2D(thief[0], thief[1], thief[2], thief[3]);
		quad.setColor(Color.black);
		engine.addtoList(quad);
		init();
	}

	@Override
	public void reShape() {
		engine.setImageSize(widthChanged, heightChanged);
	}

	@Override
	public void updateDraw(Graphics g) {
		engine.clearImageWithBackground();
		for (int i = 0; i < thief.length; i++) {
			thief[i].setX(thief[i].getX() - thief[i].getY() * dt);
			thief[i].setY(thief[i].getY() + thief[i].getX() * dt);
		}
		point.setPos(Vec2.add(point.getPos(), Vec2.scalarProd(dt, new Vec2(
				point.getPos().getY(), -point.getPos().getX()))));
		//point.setRadius(Math.sin(3 * time) * Math.sin(3 * time));
		engine.setCamera(5 * Math.cos(time) - 10, 5 * Math.cos(time) + 10, 5 * Math.sin(time) - 10,5 * Math.sin(time) + 10);
		engine.drawElements();
		engine.paintImage(g);

	}

	public static void main(String[] args) {
		new Test("Teste", 800, 500);
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

}
