package nlp.lowbow.script;

import algebra.Vec2;
import algebra.Vector;
import apps.utils.MyFrame;
import graph.Graph;
import javafx.util.Pair;
import nlp.segmentedBow.sub.SubSegmentedBow;
import nlp.seriesSummary.ArcSummarizerSpectral;
import nlp.seriesSummary.BaseArcSummarizer;
import nlp.utils.Simplex;
import twoDimEngine.TwoDimEngine;
import twoDimEngine.elements.Line2D;
import twoDimEngine.elements.Point2D;
import twoDimEngine.elements.String2D;
import twoDimEngine.shaders.ThickLineShader;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class DistributionVisualizer extends MyFrame implements MouseWheelListener {
    private TwoDimEngine engine;

    private List<Pair<Vector, Color>> pointManager;
    private Graph dataGraph;

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
    private PointVisualizationMethod pointVisualizationMethod;

    public DistributionVisualizer(String title, int width, int height) {
        super(title, width, height);
        this.pointManager = new ArrayList<>();
        this.engine = new TwoDimEngine(width, height);
        this.engine.setBackGroundColor(Color.white);
        this.engine.setCamera(-1, 1, -1, 1);
        ThickLineShader painter = new ThickLineShader(this.engine);
        painter.setThickness(0);
        this.engine.setDefaultPainter(painter);
        this.addMouseWheelListener(this);
    }


    public static void main(String[] args) {
        String seriesAddress = "C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/";
        String fileExtension = "mkv";
        String output = seriesAddress + "summary3/";
        int numberOfCluster = 6;

        // summary
        BaseArcSummarizer arcSummarizer = new ArcSummarizerSpectral(seriesAddress, fileExtension, 0.1, 0.1, 1, numberOfCluster, BaseArcSummarizer.simplexDist);
//        arcSummarizer.setHistogramDistance(arcSummarizer.getTfIdfDistance());
        arcSummarizer.buildSummary(output, 10);
        List<SubSegmentedBow> segmentedBows = arcSummarizer.getSegmentedBows();
        Map<Integer, List<Integer>> segmentIndexByClusterId = arcSummarizer.getSegmentIndexByClusterId();

        int colorRange = segmentIndexByClusterId.size();
        float colorsHSv[] = new float[colorRange];
        for (int i = 0; i < colorRange; i++) {
            colorsHSv[i] = (float) Math.random();
        }

        DistributionVisualizer distributionVisualizer = new DistributionVisualizer(seriesAddress, 500, 500);
        distributionVisualizer.setPointVisualizationMethod(distributionVisualizer.new PolygonVisual(arcSummarizer.getLowBowManager().getSimplex()));
        distributionVisualizer.setGraphData(arcSummarizer.getKnnGraph());

        for (Map.Entry<Integer, List<Integer>> entry : segmentIndexByClusterId.entrySet()) {
            for (Integer dataIndex : entry.getValue()) {
                distributionVisualizer.addDataPoint(segmentedBows.get(dataIndex - 1).getSegmentBow(), Color.getHSBColor(colorsHSv[entry.getKey()], 1.0f, 1.0f));
            }
        }
        distributionVisualizer.init();
    }

    public void addDataPoint(Vector x, Color c) {
        this.pointManager.add(new Pair<>(x, c));
    }

    public void setGraphData(Graph graph) {
        this.dataGraph = graph;
    }

    @Override
    public void reShape() {
        engine.setImageSize(widthChanged, heightChanged);
    }

    @Override
    public void updateDraw() {
        engine.clearImageWithBackground();
        pointVisualizationMethod.draw(pointManager, engine);
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


    public PointVisualizationMethod getPointVisualizationMethod() {
        return pointVisualizationMethod;
    }

    public void setPointVisualizationMethod(PointVisualizationMethod pointVisualizationMethod) {
        this.pointVisualizationMethod = pointVisualizationMethod;
    }

    private interface PointVisualizationMethod {
        void draw(List<Pair<Vector, Color>> pointManager, TwoDimEngine twoDimEngine);
    }

    // Distribution viewer
    public class PolygonVisual implements PointVisualizationMethod {
        private boolean isBuild = false;
        private Simplex simplex;

        public PolygonVisual(Simplex simplex) {
            this.simplex = simplex;
        }

        private void initialDraw(List<Pair<Vector, Color>> pointManager) {
            int n = pointManager.size();
            //generate polygon
            double period = 2 * Math.PI;
            int numWords = simplex.size();
            double step = period / numWords;
            Vec2[] polygon = new Vec2[numWords];
            double t = 0;
            for (int i = 0; i < numWords; i++) {
                polygon[i] = new Vec2(Math.cos(t), Math.sin(t));
                t += step;
            }

            for (int i = 0; i < numWords; i++) {
                Line2D line = new Line2D(polygon[i], polygon[(i + 1) % numWords]);
                line.setColor(new Color(0, 0, 0));
                engine.addtoList(line);
            }
            Vec2[] points = new Vec2[n];
            for (int i = 0; i < n; i++) {
                Pair<Vector, Color> pair = pointManager.get(i);
                Vector key = pair.getKey();
                Vec2 acm = new Vec2();
                for (int k = 0; k < numWords; k++) {
                    acm = Vec2.add(acm, Vec2.scalarProd(key.getX(k + 1), polygon[k]));
                }
                points[i] = new Vec2(acm);
            }

            if (dataGraph != null) {
                for (Integer u : dataGraph.getVertexSet()) {
                    for (Integer v : dataGraph.getAdjVertex(u)) {
                        Line2D line = new Line2D(points[u - 1], points[v - 1]);
                        line.setColor(new Color(0.1f, 0.1f, 0.1f, 0.1f));
                        engine.addtoList(line);
                    }
                }
            }

            for (int i = 0; i < n; i++) {
                Point2D point = new Point2D(points[i]);
                double radius = Math.PI / (20 * n);
                point.setRadius(radius);
                Color color = pointManager.get(i).getValue();
                point.setColor(color);
                engine.addtoList(point);
                String2D string2D = new String2D(Vec2.add(points[i], new Vec2(-2 * radius + 4 * Math.random() * radius, -2 * radius + 4 * Math.random() * radius)), "" + i);
                string2D.setColor(color);
                engine.addtoList(string2D);
            }
        }

        @Override
        public void draw(List<Pair<Vector, Color>> pointManager, TwoDimEngine twoDimEngine) {
            //set camera position
            engine.setCamera(engine.getXmin() + hMouseDisp, engine.getXmax() + hMouseDisp, engine.getYmin() + kMouseDisp, engine.getYmax() + kMouseDisp);
            if (!isBuild) {
                initialDraw(pointManager);
                isBuild = true;
            }
            kMouseDisp = 0;
            hMouseDisp = 0;
        }
    }
}
