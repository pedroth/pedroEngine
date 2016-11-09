package apps.src;

import Jama.EigenvalueDecomposition;
import algebra.src.Matrix;
import algebra.src.Vec2;
import algebra.src.Vector;
import apps.utils.MyFrame;
import graph.Graph;
import graph.GraphLaplacian;
import graph.KnnGraph;
import numeric.src.MyMath;
import twoDimEngine.BoxEngine;
import twoDimEngine.elements.Line2D;
import twoDimEngine.elements.Point2D;
import twoDimEngine.shaders.FillShader;
import twoDimEngine.shaders.PaintMethod2D;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Random;

public class DiffusionDistanceTest extends MyFrame {
    private static final int numPoints = 500;
    private static final double length = 5;
    private static final double epsilon = 0.1;
    private int knn = 5;
    private KnnGraph<Vec2> knnGraph;
    private BoxEngine engine;
    private PaintMethod2D shader;
    private ArrayList<Vec2> points;
    // index starts at 1
    private int vertexCoordinate = 1;
    private Matrix laplacian;
    private Matrix eigenVectors;
    private Vector eigenValues;
    private double heatTime = 0.1;
    /**
     * mouse coordinates
     */
    private int mx, my, newMx, newMy;

    public DiffusionDistanceTest(String title, int width, int height) {
        super(title, width, height);
        engine = new BoxEngine(width, height);
        shader = new FillShader(engine);
        engine.setBackGroundColor(Color.white);
        points = new ArrayList<>();
        Random r = new Random();
        for (int i = 0; i < numPoints; i++) {
            points.add(new Vec2(2 * length * r.nextDouble() - length, 2 * length * r.nextDouble() - length));
        }
        drawData();
        init();
    }

    public static void main(String[] args) {
        new DiffusionDistanceTest("Teste", 800, 500);
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
        if (this.knnGraph == null) {
            this.knnGraph = new KnnGraph<>(points, knn, (x, y) -> Vec2.diff(x, y).norm());
        }
        for (int i = 0; i < points.size(); i++) {
            knnGraph.putVertexProperty(i + 1, "pos", points.get(i));
        }
        drawKnnGraph(knnGraph);
        GraphLaplacian graphLaplacian = new GraphLaplacian(knnGraph, (x) -> Math.exp(-(x * x) / 25));
        laplacian = graphLaplacian.getL();
        EigenvalueDecomposition eigenvalueDecomposition = new EigenvalueDecomposition(new Jama.Matrix(laplacian.getMatrix()));
        eigenVectors = new Matrix(eigenvalueDecomposition.getV().getArray());
        eigenValues = new Vector(eigenvalueDecomposition.getRealEigenvalues());
        eigenValues.applyFunction((x) -> -x);
        drawHeatKernel();
        engine.buildBoundigBoxTree();
        engine.setCameraAuto(1.25);
    }

    private void drawHeatKernel() {
        Vector eVertex = new Vector(knnGraph.getNumVertex());
        eVertex.setX(vertexCoordinate, 1.0);
        Matrix transpose = Matrix.transpose(eigenVectors);
        Vector v = new Vector(eigenValues);
        v.applyFunction((x) -> Math.exp(x * heatTime));
        Vector f = eigenVectors.prodVector(Matrix.diag(v).prodVector(transpose.prodVector(eVertex)));
        double max = f.getMax().getX();
        double min = f.getMin().getX();
        for (int i = 0; i < points.size(); i++) {
            Point2D e = new Point2D(points.get(i));
            e.setRadius(0.1);
            double intensity = (f.getX(i + 1) - min) / (max - min);
            float[] heatColor = getHeatColor(intensity);
            e.setColor(new Color(heatColor[0], heatColor[1], heatColor[2]));
            engine.addtoList(e, shader);
        }
    }

    public float[] getHeatColor(double intensity) {
        float[] ans = new float[3];
        ans[0] = (float) MyMath.clamp((10.0 / 4.0) * intensity, 0, 1);
        ans[1] = (float) MyMath.clamp((10.0 / 4.0) * (intensity - 0.4), 0, 1);
        ans[2] = (float) MyMath.clamp(5 * (intensity - 0.8), 0, 1);
        return ans;
    }

    @Override
    public void reShape() {
        engine.setImageSize(widthChanged, heightChanged);
    }

    @Override
    public void updateDraw() {
        engine.clearImageWithBackground();
        drawData();
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
            case KeyEvent.VK_MINUS:
                heatTime -= epsilon;
                heatTime = Math.max(0, heatTime);
                System.out.println(heatTime);
                break;
            case KeyEvent.VK_PLUS:
                heatTime += epsilon;
                System.out.println(heatTime);
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
                //nothing here
        }
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
    public void mousePressed(MouseEvent e) {
        mx = e.getX();
        my = e.getY();
        Vec2 mouse = new Vec2(engine.InverseCoordX(mx), engine.InverseCoordY(my));
        double min = Double.MAX_VALUE;
        int minIndex = -1;
        for (int i = 0; i < points.size(); i++) {
            double norm = Vec2.diff(points.get(i), mouse).norm();
            if (min > norm) {
                min = norm;
                minIndex = i;
            }
        }
        vertexCoordinate = minIndex + 1;
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

}