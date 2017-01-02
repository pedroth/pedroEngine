package twoDimEngine;

import twoDimEngine.shaders.PaintMethod2D;
import twoDimEngine.shaders.Wireframe;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class AbstractEngine2D {

    /**
     * buffer with image
     */
    protected BufferedImage image;

    /**
     * background color
     */
    protected Color backGroundColor;

    protected PaintMethod2D defaultPainter;

    /**
     * camera size
     */
    protected double xmin, xmax, ymin, ymax;

    public AbstractEngine2D(int width, int height) {
        backGroundColor = Color.white;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        defaultPainter = new Wireframe(this);
    }

    public void setImageSize(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public Color getBackGroundColor() {
        return backGroundColor;
    }

    public void setBackGroundColor(Color backGroundColor) {
        this.backGroundColor = backGroundColor;
    }

    public int getWidthPxl() {
        return image.getWidth();
    }

    public int getHeightPxl() {
        return image.getHeight();
    }

    public Graphics getImageGraphics() {
        return image.getGraphics();
    }

    public void clearImageWithBackground() {
        Graphics g = image.getGraphics();
        g.setColor(backGroundColor);
        g.fillRect(0, 0, getWidthPxl(), getHeightPxl());
    }

    public void setCamera(double xmin, double xmax, double ymin, double ymax) {
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;
    }

    public double getXmin() {
        return xmin;
    }

    public double getXmax() {
        return xmax;
    }

    public double getYmin() {
        return ymin;
    }

    public double getYmax() {
        return ymax;
    }

    public PaintMethod2D getDefaultPainter() {
        return defaultPainter;
    }

    public void setDefaultPainter(PaintMethod2D painter) {
        defaultPainter = painter;
    }

    /**
     * @param g graphics of the object where image will be drawn.
     */
    public void paintImage(Graphics g) {
        g.drawImage(image, 0, 0, null);
    }

    public double integerCoordX(double x) {
        double r;
        r = this.getWidthPxl() * (x - xmin)
                / Math.abs(xmax - xmin);
        return r;
    }

    public double integerCoordY(double y) {
        double r;
        r = -this.getHeightPxl() * (y - ymax)
                / Math.abs(ymax - ymin);
        return r;
    }

    public double inverseCoordY(int y) {
        double aux, y1;
        y1 = (double) y;
        aux = y1 * (-Math.abs(ymax - ymin) / this.getHeightPxl()) + ymax;
        return aux;
    }

    public double inverseCoordX(int x) {
        double aux, x1;
        x1 = (double) x;
        aux = x1 * (Math.abs(xmax - xmin) / this.getWidthPxl()) + xmin;
        return aux;
    }

    public abstract void drawElements();

}
