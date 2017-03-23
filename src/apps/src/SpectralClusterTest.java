package apps.src;

import algebra.src.Vec2;
import apps.utils.MyFrame;
import graph.*;
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
    private static final int numPoints = 250;
    private static final double length = 5;
    private int knn = 5;
    private int kcluster = 4;
    private boolean mySpectral = false;
    private boolean isNormalized = false;
    private boolean isDiffusion = false;
    private double heatTime = 1;
    private boolean cutSegment = false;
    private KnnGraph<Vec2> knnGraph;
    private BoxEngine engine;
    private PaintMethod2D shader;
    private ArrayList<Vec2> points;
    private AbstractGraphClustering graphClustering;

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
            engine.addToList(e, shader);
        }
    }

    private void drawData() {
        engine.removeAllElements();
//        addData2Engine();
        this.knnGraph = new KnnGraph<>(points, knn, (x, y) -> Vec2.diff(x, y).squareNorm());
        for (int i = 0; i < points.size(); i++) {
            knnGraph.putVertexProperty(i + 1, "pos", points.get(i));
        }

        Map<Integer, java.util.List<Integer>> integerListMap;
        if (isDiffusion) {
            DiffusionClustering diffusionClustering = new DiffusionClustering(knnGraph);
            heatTime = 10 * diffusionClustering.getAutoHeatTime();
            integerListMap = mySpectral ? diffusionClustering.clustering(heatTime, kcluster, (x) -> Math.exp(-x), 1E-10, 50) : diffusionClustering.clusteringJama(heatTime, kcluster, (x) -> Math.exp(-x), 1E-10, 50);
            graphClustering = diffusionClustering;
        } else {
            SpectralClustering spectralClustering = new SpectralClustering(knnGraph);
            spectralClustering.setNormalized(isNormalized);
            spectralClustering.setAdrewEtAL(true);
            integerListMap = mySpectral ? spectralClustering.clustering(kcluster, (x) -> Math.exp(-x), 1E-10, 50) : spectralClustering.clusteringJama(kcluster, (x) -> Math.exp(-x), 1E-10, 50);
            graphClustering = spectralClustering;
        }
        drawKnnGraph(knnGraph);
        drawClassification(integerListMap);
        engine.buildBoundingBoxTree();
        engine.setCameraAuto(1.25);
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
                engine.addToList(e, shader);
                double var = 0.25;
                Vec2 rand = new Vec2(var * Math.random(), var * Math.random());
                String2D kclass = new String2D(Vec2.add(points.get(index - 1), rand), "" + i);
                kclass.setColor(Color.getHSBColor(colorsHSv[i], 1.0f, 1.0f));
                kclass.setFontSize(10);
                engine.addToList(kclass);
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
        if (cutSegment) {
            cutSegmentDraw();
        }
        //engine.drawTree();
        engine.drawElements();
        engine.paintImage(this.getGraphics());
    }

    private void cutSegmentDraw() {
        engine.removeAllElements(z -> z.getClass() == Line2D.class);
        if (graphClustering != null) {
            if (time % 3 < 1) {
                drawKnnGraph(knnGraph);
            } else {
                Map<Integer, Graph> clusteredGraph = graphClustering.getClusteredGraph();
                for (Map.Entry<Integer, Graph> entry : clusteredGraph.entrySet()) {
                    Graph graph = entry.getValue();
                    for (Integer v : graph.getVertexSet()) {
                        graph.putVertexProperty(v, "pos", knnGraph.getVertexProperty(v, "pos"));
                    }
                }
                clusteredGraph.forEach((x, y) -> drawKnnGraph(y));
            }
            engine.buildBoundingBoxTree();
            engine.setCameraAuto(1.25);
        }
    }

    private void drawKnnGraph(Graph knnGraph) {
        for (Integer u : knnGraph.getVertexSet()) {
            for (Integer v : knnGraph.getAdjVertex(u)) {
                Line2D line = new Line2D(knnGraph.getVertexProperty(u, "pos"), knnGraph.getVertexProperty(v, "pos"));
                line.setColor(Color.black);
                engine.addToList(line, shader);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        switch (arg0.getKeyCode()) {
            case KeyEvent.VK_P:
                graphClustering.getClusteredGraph().forEach((x, y) -> System.out.println(y.toStringGephi()));
                break;
            case KeyEvent.VK_C:
                cutSegment = !cutSegment;
                System.out.println(cutSegment);
                break;
            case KeyEvent.VK_M:
                mySpectral = !mySpectral;
                System.out.println("mySpectral: " + mySpectral);
                break;
            case KeyEvent.VK_N:
                isNormalized = !isNormalized;
                System.out.println("isNormalized: " + isNormalized);
                break;
            case KeyEvent.VK_D:
                isDiffusion = !isDiffusion;
                System.out.println("isDiffusion: " + isDiffusion);
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
            case KeyEvent.VK_W:
                heatTime += 0.1;
                System.out.println(heatTime);
                break;
            case KeyEvent.VK_S:
                heatTime -= 0.1;
                System.out.println(heatTime);
                break;
            default:
                //nothing here
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
        engine.addToList(e, shader);
        points.add(new Vec2(0.5, -0.5));
        e = new Point2D(points.get(1));
        e.setColor(Color.black);
        e.setRadius(0.01);
        engine.addToList(e, shader);
        points.add(new Vec2(0.5, 0.5));
        e = new Point2D(points.get(2));
        e.setColor(Color.black);
        e.setRadius(0.01);
        engine.addToList(e, shader);
        points.add(new Vec2(-0.5, 0.5));
        e = new Point2D(points.get(3));
        e.setColor(Color.black);
        e.setRadius(0.01);
        engine.addToList(e, shader);
        points.add(new Vec2(3, 0));
        e = new Point2D(points.get(4));
        e.setColor(Color.black);
        e.setRadius(0.01);
        engine.addToList(e, shader);
    }

}
