package nlp.lowbow.script;

import algebra.Vec2;
import algebra.Vec3;
import algebra.Vector;
import apps.utils.MyFrame;
import inputOutput.TextIO;
import nlp.lowbow.eigenLowbow.LowBowSubtitles;
import nlp.lowbow.eigenLowbow.SummaryGenLowBowManager;
import nlp.lowbow.simpleLowBow.BaseLowBow;
import nlp.lowbow.simpleLowBow.BaseLowBowManager;
import nlp.segmentedBow.sub.SubSegmentedBow;
import nlp.simpleDocModel.BaseDocModelManager;
import nlp.simpleDocModel.Bow;
import nlp.textSplitter.SubsSplitter;
import nlp.utils.EntropyStopWordPredicate;
import numeric.Camera3D;
import twoDimEngine.TwoDimEngine;
import twoDimEngine.elements.Line2D;
import twoDimEngine.shaders.ThickLineShader;
import utils.FilesCrawler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.util.Collections;
import java.util.List;


public class LowBowVisualizer<L extends BaseLowBow> extends MyFrame implements MouseWheelListener {
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

    public LowBowVisualizer(String title, int width, int height, BaseLowBowManager<L> lowBowManager) {
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

    public static void main(String[] args) throws IOException {
        //get all files
        List<String> subtitles = FilesCrawler.listFilesWithExtension("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/", "srt");
        Collections.sort(subtitles);
        List<String> videos = FilesCrawler.listFilesWithExtension("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/", "mkv");
        Collections.sort(videos);

        //construct managers
        SummaryGenLowBowManager<LowBowSubtitles, SubSegmentedBow> lowBowManager = new SummaryGenLowBowManager<>();
        BaseDocModelManager<Bow> bowManager = new BaseDocModelManager<>();

        TextIO text = new TextIO();
        SubsSplitter textSplitter = new SubsSplitter();

        //bow representation
        for (String subtitle : subtitles) {
            text.read(subtitle);
            bowManager.add(new Bow(text.getText(), textSplitter));
        }
        bowManager.build();
        //build predicate
        EntropyStopWordPredicate predicate = new EntropyStopWordPredicate(bowManager, 0.2);

        //delete
        predicate.setBowManager(null);
        bowManager = null;

        //lowbow representation
        for (int i = 0; i < subtitles.size(); i++) {
            text.read(subtitles.get(i));
            textSplitter = new SubsSplitter(predicate);
            lowBowManager.add(new LowBowSubtitles<>(text.getText(), textSplitter, videos.get(i)));
        }
        //heat model
        lowBowManager.buildModel(0.04);
        new LowBowVisualizer("OverTheGardenWall", 500, 500, lowBowManager);
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

            List<L> lowBowList = lowBowManager.getDocModels();
            for (L lowBow : lowBowList) {
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
