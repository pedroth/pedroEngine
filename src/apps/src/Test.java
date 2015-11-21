package apps.src;

import algebra.src.Vec2;
import apps.utils.MyFrame;
import twoDimEngine.BoxEngine;
import twoDimEngine.elements.Point2D;
import twoDimEngine.shaders.FillShader;
import twoDimEngine.shaders.PaintMethod2D;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Random;

public class Test extends MyFrame {
	private static final int numPoints = 100;
	private BoxEngine engine;
	private PaintMethod2D shader;
	private ArrayList<ArrayList<Integer>> graph;
	private ArrayList<Vec2> points;
	
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

	public static void main(String[] args) {
		new Test("Teste", 800, 500);
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
