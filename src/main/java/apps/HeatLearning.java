package apps;

import algebra.Matrix;
import algebra.Vec2;
import algebra.Vector;
import apps.utils.MyFrame;
import graph.DiffusionClustering;
import graph.KnnGraph;
import numeric.MyMath;
import twoDimEngine.AbstractDrawAble2D;
import twoDimEngine.BoxEngine;
import twoDimEngine.elements.Point2D;
import twoDimEngine.shaders.FillShader;
import twoDimEngine.shaders.PaintMethod2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class HeatLearning extends MyFrame implements MouseWheelListener {

    private static final double dataRadius = 0.01;

    private static final int MIN_SAMPLES = 50;

    private Point2D mousePoint = null;

    private BoxEngine engine;

    private PaintMethod2D shader;

    private List<Vec2> points = new ArrayList<>(10);
    private List<Point2D> points2D = new ArrayList<>(10);

    private List<Double> output = new ArrayList<>(10);
    private boolean isDebug = false;
    private boolean isShiftPressed = false;
    private boolean isControlPressed = false;
    private boolean isVisualMode = false;
    /*
     * mouse coordinates
     */
    private int mx, my, newMx, newMy;

    private int sqrtSamples;

    private int samples;

    private int[] colorBuffer;

    private MyModel model;

    public HeatLearning(String title, int width, int height) {
        super(title, width, height);
        this.engine = new BoxEngine(width, height);
        this.shader = new FillShader(this.engine);
        this.engine.setDefaultPainter(this.shader);
        this.engine.setBackGroundColor(Color.white);
        this.engine.setCamera(-1, 1, -1, 1);
        this.addMouseWheelListener(this);

        this.init();
        initColorBuffer(MIN_SAMPLES);
        this.model = new KnnModel();

    }

    public static void main(String[] args) {
        new HeatLearning("Teste", 800, 500);
    }

    public float[] getHeatColor(double intensity) {
        float[] ans = new float[3];
        ans[0] = (float) MyMath.clamp(10.0 / 4.0 * intensity, 0, 1);
        ans[1] = (float) MyMath.clamp(10.0 / 4.0 * (intensity - 0.6), 0, 1);
        ans[2] = (float) (MyMath.clamp(8 * (intensity - 0.9), 0, 1) + MyMath.clamp(1 - 2 * intensity, 0, 1));
        return ans;
    }

    void initColorBuffer(int sqrtSamples) {
        this.sqrtSamples = Math.min(this.widthChanged, sqrtSamples);
        this.samples = sqrtSamples * sqrtSamples;
        this.colorBuffer = new int[3 * this.samples];
        for (int i = 0; i < 3 * this.samples; i++) {
            this.colorBuffer[i] = 0;
        }
    }

    @Override
    public void reShape() {
        this.engine.setImageSize(this.widthChanged, this.heightChanged);
    }

    @Override
    public void updateDraw() {
        this.engine.clearImageWithBackground();
        if (this.isVisualMode) {
            initColorBuffer(this.sqrtSamples);
            modelCompute();
            int index;
            Graphics image = this.engine.getImageGraphics();
            for (int i = 0; i < this.sqrtSamples; i++) {
                for (int j = 0; j < this.sqrtSamples; j++) {
                    double x = 1.0 * this.widthChanged / this.sqrtSamples * i;
                    double y = 1.0 * this.heightChanged / this.sqrtSamples * j;
                    index = 3 * j + 3 * this.sqrtSamples * i;
                    this.colorBuffer[index] = Math.max(0, Math.min(255, this.colorBuffer[index]));
                    this.colorBuffer[index + 1] = Math.max(0, Math.min(255, this.colorBuffer[index + 1]));
                    this.colorBuffer[index + 2] = Math.max(0, Math.min(255, this.colorBuffer[index + 2]));
                    image.setColor(new Color(this.colorBuffer[index], this.colorBuffer[index + 1], this.colorBuffer[index + 2]));
                    double w = Math.ceil(1.0 * this.widthChanged / this.sqrtSamples);
                    double h = Math.ceil(1.0 * this.heightChanged / this.sqrtSamples);
                    image.fillRect((int) x, (int) y, (int) w, (int) h);
                }
            }
        }
        if (this.dt != 0.0) {
            Double aux = 1 / this.dt;
            NumberFormat format = new DecimalFormat("#,00");
            this.setTitle(" FPS: " + format.format(aux));
        }
        this.engine.drawElements();
        if (isDebug) {
            this.engine.drawTree();
        }
        this.engine.paintImage(this.getGraphics());
    }

    private void modelCompute() {
        int n = this.sqrtSamples;
        double xmin = this.engine.getXmin(), xmax = this.engine.getXmax();
        double ymin = this.engine.getYmin(), ymax = this.engine.getYmax();
        double dx = (xmax - xmin) / n;
        double dy = (ymax - ymin) / n;

        Vec2 x = new Vec2();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                x.setX(xmin + dx * i);
                x.setY(ymax - dy * j);
                int index = 3 * j + 3 * n * i;
                Double y = this.model.apply(x);
                this.colorBuffer[index] = (int) ((1 - y) * 255);
                this.colorBuffer[index + 1] = (int) (y * 255);
                this.colorBuffer[index + 2] = 0;
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        switch (arg0.getKeyCode()) {
            case KeyEvent.VK_SHIFT:
                this.isShiftPressed = true;
                break;
            case KeyEvent.VK_CONTROL:
                this.isControlPressed = true;
                break;
            case KeyEvent.VK_R:
                resetData();
                break;
            case KeyEvent.VK_V:
                this.isVisualMode = !this.isVisualMode;
                trainModel();
                break;
            case KeyEvent.VK_D:
                this.isDebug = !this.isDebug;
                break;
            default:
                //nothing here
        }
    }

    private void trainModel() {
        this.model.train(points, output);
    }

    private void resetData() {
        this.points.removeAll(this.points);
        this.output.removeAll(this.output);
        this.points2D.removeAll(this.points2D);
        this.mousePoint = null;
        this.engine.removeAllElements();
        this.engine.buildBoundingBoxTree();
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        switch (arg0.getKeyCode()) {
            case KeyEvent.VK_SHIFT:
                this.isShiftPressed = false;
                break;
            case KeyEvent.VK_CONTROL:
                this.isControlPressed = false;
                removeCrazy();
                break;
            default:
                break;
        }

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
        this.mx = e.getX();
        this.my = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        this.newMx = e.getX();
        this.newMy = e.getY();
        double dx = this.newMx - this.mx;
        double dy = this.newMy - this.my;
        double h = -2 * (dx / this.widthChanged);
        double k = 2 * (dy / this.heightChanged);
        Vec2 mouse = new Vec2(this.engine.inverseCoordX(this.mx), this.engine.inverseCoordY(this.my));
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (this.isControlPressed) {
                crazyStuff(mouse);
            } else {
                addPoint(mouse, this.isShiftPressed);
            }
        } else {
            double xmin = this.engine.getXmin();
            double xmax = this.engine.getXmax();
            double ymin = this.engine.getYmin();
            double ymax = this.engine.getYmax();
            double v = (xmax - xmin) * h;
            double u = (ymax - ymin) * k;
            this.engine.setCamera(xmin + v, xmax + v, ymin + u, ymax + u);
        }
        this.mx = this.newMx;
        this.my = this.newMy;
    }

    private void removeCrazy() {
        this.mousePoint.setVisible(false);
        List<Point2D> things = this.points2D;
        int size = things.size();
        for (int i = 0; i < size; i++) {
            Point2D point2D = things.get(i);
            Double color = output.get(i);
            boolean b = color > 0.5;
            point2D.setColor(new Color(b ? 0 : 255, b ? 255 : 0, 0));
        }
    }

    private void crazyStuff(Vec2 mouse) {
        int n = points.size();
        Matrix X = new Matrix(points.toArray(new Vec2[n]));
        Matrix transpose = Matrix.transpose(X);
        Vector y = transpose.prodVector(mouse);
        Matrix sigma = transpose.prod(X);
        Vector omega = Matrix.solveLinearSystem(sigma, y);

        // paint colors on data set
        List<Point2D> things = this.points2D;
        int size = things.size();
        double max = omega.getMax().getX();
        double min = omega.getMin().getX();
        System.out.println("Min: " + min + " ; Max: " + max);
        for (int i = 0; i < size; i++) {
            Point2D point2D = things.get(i);
            float[] heatColor = getHeatColor((omega.getX(i + 1) - min) / (max - min));
            point2D.setColor(new Color(heatColor[0], heatColor[1], heatColor[2]));
        }
        omega.applyFunction((z) -> (z - min) / (max - min));
        Vector ones = new Vector(omega.getDim());
        ones.fill(1.0);
        double sum = Vector.innerProd(omega, ones);
        omega = Vector.scalarProd(1.0 / sum, omega);
        Vector out = new Vector(output.toArray(new Double[output.size()]));
        double expectedV = Vector.innerProd(omega, out);
        int r = (int) MyMath.clamp((1.0 - expectedV) * 255.0, 0.0, 255.0);
        int g = (int) MyMath.clamp((expectedV * 255), 0.0, 255.0);
        int b = 0;

        // draw mouse
        if(this.mousePoint == null) {
            this.mousePoint = new Point2D(mouse);
            this.engine.addToTree(mousePoint);
        }
        this.mousePoint.setColor(new Color(r,g,b));
        double percent = 0.01;
        double sizeX = this.engine.getXmax() - this.engine.getXmin();
        this.mousePoint.setRadius(sizeX * percent);
        this.mousePoint.setPos(mouse);
        this.mousePoint.setVisible(true);
    }

    private void addPoint(Vec2 mouse, boolean out) {
        this.points.add(mouse);
        this.output.add(out ? 1.0 : 0.0);
        Point2D e = new Point2D(mouse);
        e.setRadius(dataRadius);
        e.setColor(out ? Color.green : Color.red);
        this.points2D.add(e);
        this.engine.addToTree(e);

    }

    @Override
    public void mouseMoved(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int mRotation = e.getWheelRotation();
        double percent = 0.1;
        double sizeX = engine.getXmax() - engine.getXmin();
        double sizeY = engine.getYmax() - engine.getYmin();
        engine.setCamera(engine.getXmin() - mRotation * percent * 0.5 * sizeX, engine.getXmax() + mRotation * percent * 0.5 * sizeX,
                engine.getYmin() - mRotation * percent * 0.5 * sizeY, engine.getYmax() + mRotation * percent * 0.5 * sizeY);
    }

    private interface MyModel {
        Double apply(Vec2 x);

        void train(List<Vec2> x, List<Double> y);
    }

    class KnnLeastSquare implements MyModel {
        @Override
        public Double apply(Vec2 x) {
            int n = points.size();
            if (n == 0) {
                return 0.0;
            }

            int samples = 100;
            Vec2[] randomPoints = new Vec2[samples];
            double[] randomOut = new double[samples];
            for (int i = 0; i < samples; i++) {
                int r = (int) (Math.random() * n);
                randomPoints[i] = points.get(r);
                randomOut[i] = output.get(r);
            }

            Matrix X = new Matrix(randomPoints);
            Matrix transpose = Matrix.transpose(X);
            Vector y = transpose.prodVector(x);
            Matrix sigma = transpose.prod(X);
            Vector omega = Matrix.solveLinearSystem(sigma, y);
            double max = omega.getMax().getX();
            double min = omega.getMin().getX();
            omega.applyFunction((z) -> (z - min) / (max - min));
            Vector ones = new Vector(omega.getDim());
            ones.fill(1.0);
            double sum = Vector.innerProd(omega, ones);
            omega = Vector.scalarProd(1.0 / sum, omega);
            Vector out = new Vector(randomOut);
            return Vector.innerProd(omega, out);
        }

        @Override
        public void train(List<Vec2> x, List<Double> y) {
            // empty
        }
    }

    class KnnMonteCarlo implements MyModel {
        @Override
        public Double apply(Vec2 x) {
            int index = -1;
            double minDist = Double.MAX_VALUE;
            int size = points.size();
            int monteCarloSample = 20;
            int knnSample = Integer.min(5, size);

            if (size == 0) {
                return 0.0;
            }

            double acc = 0;
            for (int k = 0; k < monteCarloSample; k++) {
                int[] sampleIndex = new int[knnSample];
                for (int i = 0; i < knnSample; i++) {
                    sampleIndex[i] = (int) (Math.random() * size);
                }

                for (int i = 0; i < knnSample; i++) {
                    int randomIndex = sampleIndex[i];
                    double dist = Vec2.diff(x, points.get(randomIndex)).norm();
                    if (minDist > dist) {
                        minDist = dist;
                        index = randomIndex;
                    }
                }
                acc += output.get(index);
            }
            return acc / monteCarloSample;
        }

        @Override
        public void train(List<Vec2> x, List<Double> y) {
            // empty
        }
    }

    class KnnModel implements MyModel {
        @Override
        public Double apply(Vec2 x) {
            int index = -1;
            double minDist = Double.MAX_VALUE;
            int size = points.size();

            if (size == 0) {
                return 0.0;
            }

            for (int i = 0; i < size; i++) {
                double dist = Vec2.diff(x, points.get(i)).norm();
                if (minDist > dist) {
                    minDist = dist;
                    index = i;
                }
            }
            return output.get(index);
        }

        @Override
        public void train(List<Vec2> x, List<Double> y) {
            //empty
        }
    }

    class KnnRandom implements MyModel {
        @Override
        public Double apply(Vec2 x) {
            int index = -1;
            double minDist = Double.MAX_VALUE;
            int size = points.size();

            if (size == 0) {
                return 0.0;
            }

            int knnSample = Integer.min(10, size);

            int[] sampleIndex = new int[knnSample];
            for (int i = 0; i < knnSample; i++) {
                sampleIndex[i] = (int) (Math.random() * size);
            }

            for (int i = 0; i < knnSample; i++) {
                int randomIndex = sampleIndex[i];
                double dist = Vec2.diff(x, points.get(randomIndex)).norm();
                if (minDist > dist) {
                    minDist = dist;
                    index = randomIndex;
                }
            }
            return output.get(index);
        }

        @Override
        public void train(List<Vec2> x, List<Double> y) { }
    }

    class DiffusionLearning implements MyModel {
        private Matrix eigenV;

        private double heatTime = 10;

        @Override
        public Double apply(Vec2 x) {
            int index = -1;
            double minDist = Double.MAX_VALUE;
            int size = points.size();

            if (size == 0) {
                return 0.0;
            }

            int knnSample = Integer.min(10, size);

            int[] sampleIndex = new int[knnSample];
            for (int i = 0; i < knnSample; i++) {
                sampleIndex[i] = (int) (Math.random() * size);
            }

            for (int i = 0; i < knnSample; i++) {
                int randomIndex = sampleIndex[i];
                double dist = Vec2.diff(x, points.get(randomIndex)).norm();
                if (minDist > dist) {
                    minDist = dist;
                    index = randomIndex;
                }
            }
            return output.get(index);
        }

        @Override
        public void train(List<Vec2> x, List<Double> y) {
            KnnGraph<Vec2> graph = new KnnGraph<>(x, 3, (p, q) -> Vec2.diff(p, q).norm());
            DiffusionClustering diffusionClustering = new DiffusionClustering(graph);
            diffusionClustering.clusteringJama(this.heatTime, 2, d -> Math.exp(-(d * d / 2)), 0.01, 10);
        }
    }

}