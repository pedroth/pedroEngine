package apps;

import java.applet.Applet;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JFrame;

public class FrameApplet extends Applet implements ComponentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JFrame frame;

	void destroyFrame() {
		if (frame != null)
			frame.dispose();
		frame = null;
		repaint();
	}

	boolean started = false;

	public void init() {
		addComponentListener(this);
	}

	void showFrame() {
		if (frame == null) {
			started = true;
			frame = new ImplicitSurface("Implicit Surface", 700, 700);
			repaint();
		}
	}

	public void paint(Graphics g) {
		String s = "Applet is open in a separate window.";
		if (!started)
			s = "Applet is starting.";
		else if (frame == null)
			s = "Applet is finished.";
		else
			frame.repaint();
		g.drawString(s, 10, 30);
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentShown(ComponentEvent e) {
		showFrame();
	}

	public void componentResized(ComponentEvent e) {
	}

	public void destroy() {
		if (frame != null)
			frame.dispose();
		frame = null;
		repaint();
	}
}
