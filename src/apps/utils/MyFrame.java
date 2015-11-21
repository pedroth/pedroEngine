package apps.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public abstract class MyFrame extends JFrame implements KeyListener,
		MouseListener, MouseMotionListener {
	/**
	 * width of the  window in pixels
	 */
	protected int widthChanged;
	/**
	 * height of the window in pixels
	 */
	protected int heightChanged;
	protected double time;
	/**
	 * time differential between frames in seconds
	 */
	protected double dt;
	/**
	 * time handling variables
	 *
	 * time is in seconds
	 */
	private double oldTime;
	private int fps;
	private int fpsCount;
	
	private boolean isApplet = false;


	public MyFrame(String title, int width, int height) {
		// Set JFrame title
		super(title);

		// Set default close operation for JFrame
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set JFrame size
		setSize(width, height);

		widthChanged = width;
		heightChanged = height;

		this.addKeyListener(this);

		this.addMouseListener(this);
		this.addMouseMotionListener(this);

		/**
		 * time in seconds
		 */
		oldTime = (System.currentTimeMillis()) * 1E-03;
		fps = 0;
		fpsCount = 0;
	}
	
	public void init() {
		//Make JFrame visible
		this.setVisible(true);
	}

	/**
	 * function to handle the resize of the window
	 */
	public abstract void reShape();

	private void updateTime(double dt) {
		time += dt;
	}
	
	public void resetTime() {
		time = 0;
	}

	/**
	 * 
	 * @return time in seconds between to calls of this function;
	 */
	private double getTimeDifferential() {
		double currentTime = (System.currentTimeMillis()) * 1E-03;
		double dt = currentTime - oldTime;
		oldTime = currentTime;
		return dt;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public double getDt() {
		return dt;
	}

	public void setDt(double dt) {
		this.dt = dt;
	}

	public int getFps() {
		return fps;
	}

	public void setFps(int fps) {
		this.fps = fps;
	}

	public int getWidthChanged() {
		return widthChanged;
	}

	public void setWidthChanged(int widthChanged) {
		this.widthChanged = widthChanged;
	}

	public int getHeightChanged() {
		return heightChanged;
	}

	public void setHeightChanged(int heightChanged) {
		this.heightChanged = heightChanged;
	}
	
	public boolean isApplet() {
		return isApplet;
	}

	public void setApplet(boolean isApplet) {
		this.isApplet = isApplet;
		if(!isApplet)
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		else
			this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}

	/**
	 * this method is automatically called every time the window needs to be
	 * painted
	 * 
	 * this is the main of a graphics application
	 * 
	 * g is the graphics object of the window(not the canvas image)
	 * 
	 * this is a override of the JFrame class
	 */
	public void paint(Graphics g) {
		if (Math.abs(widthChanged - this.getWidth()) > 0
				|| Math.abs(heightChanged - this.getHeight()) > 0) {
			/**
			 * getting the actual size of the window and updating the variables.
			 */
			widthChanged = this.getWidth();
			heightChanged = this.getHeight();
			reShape();
		}
		update(g);
	}

	/**
	 * g is the graphics object of the window(not the canvas image)
	 * 
	 * this is a override of the JFrame class
	 * 
	 * this is where the actual drawing will be made.
	 */
	public void update(Graphics g) {
		dt = getTimeDifferential();
		updateTime(dt);
		updateDraw(g);
		/**
		 * fps count
		 */
		if(Math.floor(time + dt) - Math.floor(time) > 0) {
			fps = fpsCount;
			fpsCount = 0;
		}
		fpsCount++;
		repaint();
	}

	/**
	 * 
	 * @param g
	 *            Graphics of the frame
	 */
	public abstract void updateDraw(Graphics g);
}
