package apps.src;

import algebra.src.Vec2;
import apps.utils.MyFrame;
import numeric.src.MyMath;
import twoDimEngine.BoxEngine;
import twoDimEngine.elements.Point2D;
import twoDimEngine.shaders.FillShader;
import twoDimEngine.shaders.PaintMethod2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class HeatLearning extends MyFrame {

    private static final double dataRadius = 0.01;
    private static final int MIN_SAMPLES = 100;
    private BoxEngine engine;
    private PaintMethod2D shader;
    private List<Vec2> points = new ArrayList<>(10);
    private List<Double> output = new ArrayList<>(10);
    private final MyModel knnMonteCarlo = new MyModel() {
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
    };
    private final MyModel knn = new MyModel() {
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
    };
    private final MyModel knnRandom = new MyModel() {
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

        }
    };
    private boolean isShiftPressed = false;

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
        this.init();
        initColorBuffer(MIN_SAMPLES);
        this.model = knnMonteCarlo;
    }

    public static void main(String[] args) {
        new HeatLearning("Teste", 800, 500);
    }

    public float[] getHeatColor(double intensity) {
        float[] ans = new float[3];
        ans[0] = (float) MyMath.clamp((10.0 / 4.0) * intensity, 0, 1);
        ans[1] = (float) MyMath.clamp((10.0 / 4.0) * (intensity - 0.4), 0, 1);
        ans[2] = (float) MyMath.clamp(5 * (intensity - 0.8), 0, 1);
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
            case KeyEvent.VK_R:
                resetData();
                break;
            case KeyEvent.VK_V:
                this.isVisualMode = !this.isVisualMode;
                trainModel();
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
        this.engine.removeAllElements();
        this.engine.buildBoundigBoxTree();
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        if (arg0.getKeyCode() == KeyEvent.VK_SHIFT) {
            this.isShiftPressed = false;
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
        if (SwingUtilities.isLeftMouseButton(e)) {
            Vec2 mouse = new Vec2(this.engine.inverseCoordX(this.mx), this.engine.inverseCoordY(this.my));
            addPoint(mouse, this.isShiftPressed);
        } else {
            this.engine.setCamera(this.engine.getXmin() + h, this.engine.getXmax() + h, this.engine.getYmin() + k, this.engine.getYmax() + k);
        }
        this.mx = this.newMx;
        this.my = this.newMy;
    }

    private void addPoint(Vec2 mouse, boolean out) {
        this.points.add(mouse);
        this.output.add(out ? 1.0 : 0.0);
        Point2D e = new Point2D(mouse);
        e.setRadius(dataRadius);
        e.setColor(out ? Color.green : Color.red);
        this.engine.addtoList(e);
        this.engine.buildBoundigBoxTree();
    }

    @Override
    public void mouseMoved(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }


    private interface MyModel {
        Double apply(Vec2 x);

        void train(List<Vec2> x, List<Double> y);
    }

}