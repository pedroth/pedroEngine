package apps.src;

import algebra.src.Vec2;
import apps.utils.MyFrame;
import graph.Graph;
import graph.KnnGraph;
import graph.SpectralClustering;
import twoDimEngine.BoxEngine;
import twoDimEngine.elements.Line2D;
import twoDimEngine.elements.Point2D;
import twoDimEngine.elements.String2D;
import twoDimEngine.shaders.FillShader;
import twoDimEngine.shaders.PaintMethod2D;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SpectralClusterTest extends MyFrame {
    private static final int numPoints = 100;
    private static final double length = 5;
    private int knn = 5;
    private int kcluster = 6;
    private boolean mySpectral = false;
    private boolean isNormalized = false;
    private KnnGraph<Vec2> knnGraph;
    private BoxEngine engine;
    private PaintMethod2D shader;
    private ArrayList<Vec2> points;

    public SpectralClusterTest(String title, int width, int height) {
        super(title, width, height);
        engine = new BoxEngine(width, height);
        shader = new FillShader(engine);
        engine.setBackGroundColor(Color.white);
        points = new ArrayList<>();
        Random r = new Random();
        for (int i = 0; i < numPoints; i++) {
            points.add(new Vec2(2 * length * r.nextDouble() - length, 2 * length * r.nextDouble() - length));
        }
//        addData();
        drawData();
        init();
    }

    public static void main(String[] args) {
        new SpectralClusterTest("Teste", 800, 500);
    }

    private void addData2Engine() {
        for (int i = 0; i < points.size(); i++) {
            Point2D e = new Point2D(points.get(i));
            e.setColor(Color.black);
            e.setRadius(0.01);
            engine.addtoList(e, shader);
        }
    }

    private void drawData() {
        engine.removeAllElements();
        addData2Engine();
        engine.buildBoundigBoxTree();
        engine.setCameraAuto(1.25);
        this.knnGraph = new KnnGraph<>(points, knn, (x, y) -> Vec2.diff(x, y).squareNorm());
        for (int i = 0; i < points.size(); i++) {
            knnGraph.putVertexProperty(i + 1, "pos", points.get(i));
        }
        SpectralClustering spectralClustering = new SpectralClustering(knnGraph);
        spectralClustering.setNormalized(isNormalized);
        Map<Integer, java.util.List<Integer>> integerListMap = mySpectral ? spectralClustering.clustering(kcluster, (x) -> Math.exp(-x), 1E-10, 500) : spectralClustering.clusteringJama(kcluster, (x) -> Math.exp(-x), 1E-10, 500);
        drawKnnGraph(knnGraph);
        drawClassification(integerListMap);
        engine.buildBoundigBoxTree();
    }

    private void drawClassification(Map<Integer, List<Integer>> inverseClassification) {
        int colorRange = inverseClassification.size();
        float colorsHSv[] = new float[colorRange];
        for (int i = 0; i < colorRange; i++) {
            colorsHSv[i] = (float) Math.random();
        }

        int i = 0;
        for (Map.Entry<Integer, List<Integer>> entry : inverseClassification.entrySet()) {
            for (Integer index : entry.getValue()) {
                Point2D e = new Point2D(points.get(index - 1));
                e.setRadius(0.1);
                e.setColor(Color.getHSBColor(colorsHSv[i], 1.0f, 1.0f));
                engine.addtoList(e, shader);
                double var = 0.25;
                Vec2 rand = new Vec2(var * Math.random(), var * Math.random());
                String2D kclass = new String2D(Vec2.add(points.get(index - 1), rand), "" + i);
                kclass.setColor(Color.getHSBColor(colorsHSv[i], 1.0f, 1.0f));
                kclass.setFontSize(10);
                engine.addtoList(kclass);
            }
            i++;
        }
    }

    @Override
    public void reShape() {
        engine.setImageSize(widthChanged, heightChanged);
    }

    @Override
    public void updateDraw() {
        engine.clearImageWithBackground();
//        engine.drawTree();
        engine.drawElements();
        engine.paintImage(this.getGraphics());
    }

    private void drawKnnGraph(Graph knnGraph) {
        for (Integer u : knnGraph.getVertexSet()) {
            for (Integer v : knnGraph.getAdjVertex(u)) {
                Line2D line = new Line2D(knnGraph.getVertexProperty(u, "pos"), knnGraph.getVertexProperty(v, "pos"));
                line.setColor(Color.black);
                engine.addtoList(line, shader);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        switch (arg0.getKeyCode()) {
            case KeyEvent.VK_M:
                mySpectral = !mySpectral;
                System.out.println("mySpectral: " + mySpectral);
                break;
            case KeyEvent.VK_N:
                isNormalized = !isNormalized;
                System.out.println("isNormalized: " + isNormalized);
                break;
            case KeyEvent.VK_MINUS:
                kcluster--;
                System.out.println(kcluster);
                break;
            case KeyEvent.VK_PLUS:
                kcluster++;
                System.out.println(kcluster);
                break;
            case KeyEvent.VK_1:
                knn = 1;
                break;
            case KeyEvent.VK_2:
                knn = 2;
                break;
            case KeyEvent.VK_3:
                knn = 3;
                break;
            case KeyEvent.VK_4:
                knn = 4;
                break;
            case KeyEvent.VK_5:
                knn = 5;
                break;
            case KeyEvent.VK_6:
                knn = 6;
                break;
            case KeyEvent.VK_7:
                knn = 7;
                break;
            case KeyEvent.VK_8:
                knn = 8;
                break;
            case KeyEvent.VK_9:
                knn = 9;
                break;
            default:
                knn = 10;
        }
        drawData();

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

    private void addData() {
        points.add(new Vec2(-0.5, -0.5));
        Point2D e = new Point2D(points.get(0));
        e.setColor(Color.black);
        e.setRadius(0.01);
        engine.addtoList(e, shader);
        points.add(new Vec2(0.5, -0.5));
        e = new Point2D(points.get(1));
        e.setColor(Color.black);
        e.setRadius(0.01);
        engine.addtoList(e, shader);
        points.add(new Vec2(0.5, 0.5));
        e = new Point2D(points.get(2));
        e.setColor(Color.black);
        e.setRadius(0.01);
        engine.addtoList(e, shader);
        points.add(new Vec2(-0.5, 0.5));
        e = new Point2D(points.get(3));
        e.setColor(Color.black);
        e.setRadius(0.01);
        engine.addtoList(e, shader);
        points.add(new Vec2(3, 0));
        e = new Point2D(points.get(4));
        e.setColor(Color.black);
        e.setRadius(0.01);
        engine.addtoList(e, shader);
    }

}
