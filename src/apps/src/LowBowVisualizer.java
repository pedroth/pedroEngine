package apps.src;

import algebra.src.Vec2;
import algebra.src.Vec3;
import algebra.src.Vector;
import apps.utils.MyFrame;
import inputOutput.MyText;
import nlp.lowbow.src.simpleLowBow.BaseLowBowManager;
import nlp.lowbow.src.simpleLowBow.LowBow;
import nlp.textSplitter.SubsSplitter;
import numeric.src.Camera3D;
import twoDimEngine.TwoDimEngine;
import twoDimEngine.elements.Line2D;
import twoDimEngine.shaders.ThickLineShader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;

/**
 * Created by Pedroth on 12/28/2015.
 */
public class LowBowVisualizer extends MyFrame implements MouseWheelListener {
    private TwoDimEngine engine;
    /**
     * Camera
     */
    private Camera3D camera;

    private BaseLowBowManager lowBowManager;
    /**
     * mouse coordinates
     */
    private int mx, my, newMx, newMy;
    private double hMouseDisp;
    private double kMouseDisp;
    private double dxMouse;
    private double dyMouse;
    private double rotationForce = 10.0;
    private double dispSpeed = 1;
    /**
     * lowbow vizualizer
     */
    private LowBowVisualizationMethod lowBowVisualizationMethod;

    public LowBowVisualizer(String title, int width, int height, BaseLowBowManager lowBowManager) {
        super(title, width, height);
        this.engine = new TwoDimEngine(width, height);
        this.engine.setBackGroundColor(Color.white);
        this.engine.setCamera(-1, 1, -1, 1);
        ThickLineShader painter = new ThickLineShader(this.engine);
        painter.setThickness(3);
        this.engine.setDefaultPainter(painter);
        this.camera = new Camera3D();
        this.lowBowManager = lowBowManager;
        this.lowBowVisualizationMethod = new PolygonVisual();
        this.addMouseWheelListener(this);
        this.init();
    }

    public static void main(String[] args) {
        BaseLowBowManager lowBowManager = new BaseLowBowManager();
        MyText text = new MyText();
        text.read("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/OverTheGardenWall1.srt");
        LowBow lowbowSubtitleEigen = new LowBow(text.getText(), new SubsSplitter());
        lowBowManager.add(lowbowSubtitleEigen);
        lowbowSubtitleEigen.build();
        new LowBowVisualizer("", 500, 500, lowBowManager);
    }

    @Override
    public void reShape() {
        engine.setImageSize(widthChanged, heightChanged);
    }

    @Override
    public void updateDraw() {
        engine.clearImageWithBackground();
        lowBowVisualizationMethod.draw(lowBowManager, engine);
        engine.drawElements();
        this.setTitle("Fps : " + this.getFps());
        engine.paintImage(this.getGraphics());
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        mx = e.getX();
        my = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Vec3 thrust = camera.getThrust();
        thrust.setX(0);
        thrust.setY(0);
        thrust.setZ(0);
        kMouseDisp = 0;
        hMouseDisp = 0;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        newMx = e.getX();
        newMy = e.getY();

        dxMouse = newMx - mx;
        dyMouse = newMy - my;

        Vec3 raw = camera.getRaw();
        Vec3 thrust = camera.getThrust();

        if (SwingUtilities.isLeftMouseButton(e)) {
            raw.setY(raw.getY() + 2 * Math.PI * (dxMouse / widthChanged));
            raw.setZ(raw.getZ() + 2 * Math.PI * (dyMouse / heightChanged));
            thrust.setY(rotationForce * 2 * Math.PI * (dxMouse / widthChanged));
            thrust.setZ(rotationForce * 2 * Math.PI * (dyMouse / heightChanged));
        } else {
            raw.setX(raw.getX() + (dxMouse / widthChanged) + (dyMouse / heightChanged));
            thrust.setX(rotationForce * ((dxMouse / widthChanged) + (dyMouse / heightChanged)));
        }

        hMouseDisp = -dispSpeed * (dxMouse / widthChanged);
        kMouseDisp = dispSpeed * (dyMouse / heightChanged);

        mx = newMx;
        my = newMy;
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int mRotation = e.getWheelRotation();
        double percent = 0.1;
        double sizeX = engine.getXmax() - engine.getXmin();
        double sizeY = engine.getYmax() - engine.getYmin();
        engine.setCamera(engine.getXmin() - mRotation * percent * 0.5 * sizeX, engine.getXmax() + mRotation * percent * 0.5 * sizeX, engine.getYmin() - mRotation * percent * 0.5 * sizeY, engine.getYmax() + mRotation * percent * 0.5 * sizeY);
    }


    public LowBowVisualizationMethod getLowBowVisualizationMethod() {
        return lowBowVisualizationMethod;
    }

    public void setLowBowVisualizationMethod(LowBowVisualizationMethod lowBowVisualizationMethod) {
        this.lowBowVisualizationMethod = lowBowVisualizationMethod;
    }

    private interface LowBowVisualizationMethod {
        void draw(BaseLowBowManager lowBowManager, TwoDimEngine twoDimEngine);
    }


    /// LowBow Visualization Methods

    public class PolygonVisual implements LowBowVisualizationMethod {
        private boolean isBuild = false;

        @Override
        public void draw(BaseLowBowManager lowBowManager, TwoDimEngine twoDimEngine) {
            //set camera position
            engine.setCamera(engine.getXmin() + hMouseDisp, engine.getXmax() + hMouseDisp, engine.getYmin() + kMouseDisp, engine.getYmax() + kMouseDisp);
            if (!isBuild) {
                initialDraw(lowBowManager, twoDimEngine);
                isBuild = true;
            }
            kMouseDisp = 0;
            hMouseDisp = 0;
        }

        private void initialDraw(BaseLowBowManager lowBowManager, TwoDimEngine twoDimEngine) {
            //generate polygon
            double period = 2 * Math.PI;
            int numWords = lowBowManager.getSimplex().size();
            double step = period / numWords;
            Vec2[] polygon = new Vec2[numWords];
            double t = 0;
            for (int i = 0; i < numWords; i++) {
                polygon[i] = new Vec2(Math.cos(t), Math.sin(t));
                t += step;
            }

            List<LowBow> lowBowList = lowBowManager.getLowbows();
            for (LowBow lowBow : lowBowList) {
                Color hsbColor = Color.getHSBColor((float) Math.random(), 1.0f, 1.0f);
                Vector[] curve = lowBow.getCurve();
                Vec2[] baryCurve = new Vec2[curve.length];
                for (int j = 0; j < curve.length; j++) {
                    Vec2 acm = new Vec2();
                    for (int k = 0; k < numWords; k++) {
                        acm = Vec2.add(acm, Vec2.scalarProd(curve[j].getX(k + 1), polygon[k]));
                    }
                    baryCurve[j] = acm;
                }

                for (int j = 0; j < curve.length - 1; j++) {
                    Line2D line = new Line2D(baryCurve[j], baryCurve[j + 1]);
                    line.setColor(hsbColor);
                    engine.addtoList(line);
                }
            }
        }
    }

}
