package apps.src;

import algebra.src.Vec2;
import apps.utils.MyFrame;
import graph.Graph;
import graph.KnnGraph;
import graph.SpectralClustering;
import twoDimEngine.BoxEngine;
import twoDimEngine.elements.Line2D;
import twoDimEngine.elements.Point2D;
import twoDimEngine.shaders.FillShader;
import twoDimEngine.shaders.PaintMethod2D;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Test extends MyFrame {
    private static final int numPoints = 50;
    private static final int knn = 3;
    private KnnGraph<Vec2> knnGraph;
    private BoxEngine engine;
    private PaintMethod2D shader;
    private ArrayList<Vec2> points;

    public Test(String title, int width, int height) {
        super(title, width, height);
        engine = new BoxEngine(width, height);
        shader = new FillShader(engine);
        engine.setBackGroundColor(Color.white);
        engine.setCamera(-2, 2, -2, 2);
        points = new ArrayList<>();
        Random r = new Random();
        for (int i = 0; i < numPoints; i++) {
            points.add(new Vec2(2 * r.nextDouble() - 1, 2 * r.nextDouble() - 1));
            Point2D e = new Point2D(points.get(i));
            e.setColor(Color.black);
            e.setRadius(0.01);
            engine.addtoList(e, shader);
        }
//        addData();
        engine.buildBoundigBoxTree();
        this.knnGraph = new KnnGraph<>(points, 5, (x, y) -> Vec2.diff(x, y).squareNorm());
        for (int i = 0; i < points.size(); i++) {
            knnGraph.putVertexProperty(i + 1, "pos", points.get(i));
        }
        SpectralClustering spectralClustering = new SpectralClustering(knnGraph);
        Map<Integer, java.util.List<Integer>> integerListMap = spectralClustering.clustering(knn, (x) -> Math.exp(-x));
        drawKnnGraph(knnGraph);
        drawClassification(integerListMap);
        engine.buildBoundigBoxTree();
        init();
    }

    public static void main(String[] args) {
        new Test("Teste", 800, 500);
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
                Point2D e = new Point2D(points.get(index));
                e.setRadius(0.1);
                e.setColor(Color.getHSBColor(colorsHSv[i], 1.0f, 1.0f));
                engine.addtoList(e, shader);
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
        // TODO Auto-generated method stub

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
