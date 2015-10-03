package apps;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Random;

import twoDimEngine.BoxEngine;
import twoDimEngine.FillShader;
import twoDimEngine.PaintMethod2D;
import twoDimEngine.Point2D;
import algebra.Vec2;

public class Test extends MyFrame {
	private BoxEngine engine;
	private PaintMethod2D shader;
	private ArrayList<ArrayList<Integer>> graph;
	private ArrayList<Vec2> points;
	private static final int numPoints = 100;
	
	public Test(String title, int width, int height) {
		super(title, width, height);
		engine = new BoxEngine(width, height);
		shader = new FillShader(engine);
		engine.setBackGroundColor(Color.white);
		engine.setCamera(-1, 1, -1, 1);
		graph = new ArrayList<ArrayList<Integer>>();
		points = new ArrayList<Vec2>();
		Random r = new Random();
		for(int i = 0; i < numPoints; i++) {
			points.add(new Vec2(2 * r.nextDouble() - 1, 2 * r.nextDouble() - 1));
			graph.add(new ArrayList<Integer>());
			Point2D e = new Point2D(points.get(i));
			e.setColor(Color.black);
			e.setRadius(0.01);
			engine.addtoList(e, shader);
		}
		init();
	}

	@Override
	public void reShape() {
		engine.setImageSize(widthChanged, heightChanged);
	}

	@Override
	public void updateDraw(Graphics g) {
		engine.clearImageWithBackground();
		engine.buildBoundigBoxTree();
			
		engine.drawTree();
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
