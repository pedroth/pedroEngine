package other;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

public class FRMMain extends JFrame implements KeyListener {
    private final List<Shape> list = new ArrayList<>();

    private boolean paintPhase = true;

    public FRMMain() {
        this.setUndecorated(true);
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(screenSize.width, screenSize.height);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setBackground(new Color(0, 0, 0, 1));
        this.setOpacity(1f);
        this.setAlwaysOnTop(true);
        this.setVisible(true);
        enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
        this.addKeyListener(this);
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        if (paintPhase && e.getID() == MouseEvent.MOUSE_RELEASED) {
            paintPhase = true;
        }
        super.processMouseEvent(e);
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent event) {
        if (paintPhase && event.getID() == MouseEvent.MOUSE_DRAGGED) {
            int x = event.getX();
            int y = event.getY();
            list.add(new Ellipse2D.Float(x, y, 8, 8));
            repaint();
        }
        super.processMouseMotionEvent(event);
    }

    @Override
    public boolean contains(int x, int y) {
        return paintPhase;
    }

    public static void main(String[] args) {
        new FRMMain();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D gfx = (Graphics2D) g;
        gfx.setColor(Color.RED);
        for (Shape s : list) {
            gfx.draw(s);
        }
        this.setAlwaysOnTop(true);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}