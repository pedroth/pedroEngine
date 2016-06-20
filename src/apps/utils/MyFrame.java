package apps.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

/**
 * The type My frame.
 */
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
    /**
     * The Time.
     */
    protected double time;
    /**
     * time differential between frames in seconds
     */
    protected double dt;
    /**
     * time handling variables
     * <p>
     * time is in seconds
     */
    private double oldTime;
    private int fps;
    private int fpsCount;

    private boolean isApplet = false;


    /**
     * Instantiates a new My frame.
     *
     * @param title  the title
     * @param width  the width
     * @param height the height
     */
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

    /**
     * Init void.
     */
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

    /**
     * Reset time.
     */
    public void resetTime() {
        time = 0;
    }

    /**
     * @return time in seconds between to calls of this function;
     */
    private double getTimeDifferential() {
        double currentTime = (System.currentTimeMillis()) * 1E-03;
        double dt = currentTime - oldTime;
        oldTime = currentTime;
        return dt;
    }

    /**
     * Gets time.
     *
     * @return the time
     */
    public double getTime() {
        return time;
    }

    /**
     * Sets time.
     *
     * @param time the time
     */
    public void setTime(double time) {
        this.time = time;
    }

    /**
     * Gets dt.
     *
     * @return the dt
     */
    public double getDt() {
        return dt;
    }

    /**
     * Sets dt.
     *
     * @param dt the dt
     */
    public void setDt(double dt) {
        this.dt = dt;
    }

    /**
     * Gets fps.
     *
     * @return the fps
     */
    public int getFps() {
        return fps;
    }

    /**
     * Sets fps.
     *
     * @param fps the fps
     */
    public void setFps(int fps) {
        this.fps = fps;
    }

    /**
     * Gets width changed.
     *
     * @return the width changed
     */
    public int getWidthChanged() {
        return widthChanged;
    }

    /**
     * Sets width changed.
     *
     * @param widthChanged the width changed
     */
    public void setWidthChanged(int widthChanged) {
        this.widthChanged = widthChanged;
    }

    /**
     * Gets height changed.
     *
     * @return the height changed
     */
    public int getHeightChanged() {
        return heightChanged;
    }

    /**
     * Sets height changed.
     *
     * @param heightChanged the height changed
     */
    public void setHeightChanged(int heightChanged) {
        this.heightChanged = heightChanged;
    }

    /**
     * Is applet.
     *
     * @return the boolean
     */
    public boolean isApplet() {
        return isApplet;
    }

    /**
     * Sets applet.
     *
     * @param isApplet the is applet
     */
    public void setApplet(boolean isApplet) {
        this.isApplet = isApplet;
        if (!isApplet)
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        else
            this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    /**
     * this method is automatically called every time the window needs to be
     * painted
     * <p>
     * this is the main of a graphics application
     * <p>
     * g is the graphics object of the window(not the canvas image)
     * <p>
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
     * <p>
     * this is a override of the JFrame class
     * <p>
     * this is where the actual drawing will be made.
     */
    public void update(Graphics g) {
        dt = getTimeDifferential();
        updateTime(dt);
        updateDraw();
        /**
         * fps count
         */
        if (Math.floor(time + dt) - Math.floor(time) > 0) {
            fps = fpsCount;
            fpsCount = 0;
        }
        fpsCount++;
        repaint();
    }

    /**
     * Draw image.
     *
     * @param image the image
     */
    public void drawImage(BufferedImage image) {
        this.getGraphics().drawImage(image, 0, 0, null);
    }


    /**
     * Update draw.
     */
    public abstract void updateDraw();
}
