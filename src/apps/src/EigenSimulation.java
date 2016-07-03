package apps.src;

import algebra.src.*;
import apps.utils.MyFrame;
import numeric.src.Camera3D;
import numeric.src.MyMath;
import numeric.src.SymmetricEigen;
import twoDimEngine.TwoDimEngine;
import twoDimEngine.elements.String2D;
import twoDimEngine.shaders.ThickLineShader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Eigen simulation.
 */
public class EigenSimulation extends MyFrame {
    /*
     * Ray trace Image parameters
     */
    private static final int MIN_SAMPLES = 100;
    /*
     * max ray trace vision
     */
    private static final int MAX_SAMPLES = 100;
    /**
     * The Max vision.
     */
/*
     * The Max vision.
     */
    double maxVision = 10.0;
    private int sqrtSamples;
    private int samples;
    private int[] colorBuffer;
    /*
     * Engine
     */
    private TwoDimEngine engine;
    private String2D fps;
    /*
     * Symmetric Matrix
     */
    private Matrix symMatrix;
    private Double[] eigenValues;
    private Vector[] eigenVector;
    /*
     * Camera
     */
    private Camera3D camera;
    /*
     * Spheres array
     */
    private List<Sphere> spheres = new ArrayList<>(1);
    /*
     * mouse coordinates
     */
    private int mx, my, newMx, newMy;

    /**
     * Instantiates a new My frame.
     *
     * @param title  the title
     * @param width  the width
     * @param height the height
     */
    public EigenSimulation(String title, int width, int height, Matrix symMatrix) {
        super(title, width, height);

        //init engine
        this.engine = new TwoDimEngine(width, height);
        this.engine.setBackGroundColor(Color.black);
        this.engine.setCamera(-1, 1, -1, 1);

        //set default shader
        ThickLineShader painter = new ThickLineShader(this.engine);
        painter.setThickness(3);
        this.engine.setDefaultPainter(painter);

        //add fps string
        fps = new String2D(new Vec2(-0.95, 0.83), "0");
        fps.setColor(Color.white);
        engine.addtoList(fps, painter);

        // Add principal sphere
        Sphere principalSphere = new Sphere(new Vec3(0, 0, 0), 1.0, 0);
        principalSphere.setSphereShader(new DirectionShader());
        spheres.add(principalSphere);


        //init camera
        this.camera = new Camera3D();
        this.camera.setRaw(new Vec3(3, 0, 0));
        this.camera.update(0);
        //init Image
        initColorBuffer(MIN_SAMPLES);

        //init matrix
        this.symMatrix = symMatrix.getSubMatrix(1, 3, 1, 3);
        SymmetricEigen symmetricEigen = new SymmetricEigen(this.symMatrix);
        symmetricEigen.orderEigenValuesAndVector();
        eigenValues = symmetricEigen.getEigenValues();
        eigenVector = symmetricEigen.getEigenVectors();

        //display frame
        this.init();
    }

    public static void main(String[] args) {
        Matrix symMatrix = new Matrix(3, 3);
        symMatrix.fillRandom(-10, 10);
        symMatrix = Matrix.add(symMatrix, Matrix.transpose(symMatrix));
        new EigenSimulation("Eigen Simulation", 500, 500, new TridiagonalMatrix(new double[]{4, 2, 1}, new double[]{0, 0}, new double[]{0, 0}));
    }

    /**
     * Init color buffer.
     *
     * @param sqrtSamples the sqrt samples
     */
    void initColorBuffer(int sqrtSamples) {
        this.sqrtSamples = Math.min(widthChanged, sqrtSamples);
        this.samples = sqrtSamples * sqrtSamples;
        colorBuffer = new int[3 * samples];
        for (int i = 0; i < 3 * samples; i++) {
            colorBuffer[i] = 0;
        }
    }

    @Override
    public void reShape() {
        engine.setImageSize(widthChanged, heightChanged);
    }

    private double getSphereIntersection(Vector eye, Vector dir, Sphere sphere) {
        Vector p = sphere.getPos();
        double r = sphere.getRadius();
        Vector diff = Vector.diff(eye, p);
        double c = diff.squareNorm() - r * r;
        double b = 2 * Vector.innerProd(dir, diff);
        double a = dir.squareNorm();
        double discr = b * b - 4 * a * c;

        if (discr < 0) {
            return Double.MAX_VALUE;
        }

        double sol1 = (-b - Math.sqrt(discr)) / (2 * a);
        double sol2 = (-b + Math.sqrt(discr)) / (2 * a);
        double m = sol1 * sol2;
        if (m < 0) {
            return Math.max(sol1, sol2);
        } else if (m == 0) {
            return 0;
        } else {
            return sol1 > 0 && sol2 > 0 ? Math.min(sol1, sol2) : Double.MAX_VALUE;
        }
    }

    /**
     * Shade vec 3.
     *
     * @param inter  the inter
     * @param sphere the sphere
     * @return the vec 3
     */
    Vec3 shade(Vec3 inter, Sphere sphere) {
        if (sphere == null) {
            Color backGroundColor = engine.getBackGroundColor();
            return new Vec3(backGroundColor.getRed(), backGroundColor.getGreen(), backGroundColor.getBlue());
        }
        return sphere.getColor(inter);
    }

    /**
     * Return a point p where p = init + dir * t
     *
     * @param init the init
     * @param dir  the dir
     * @param t    the t
     * @return init + dir * t
     */
    Vector line(Vector init, Vector dir, double t) {
        return Vector.add(init, Vector.scalarProd(t, dir));
    }

    /**
     * @param dir
     * @return Color in the direction dir
     */
    private Vec3 trace(Vector dir) {
        Vector eye = camera.getEye();
        int index = -1;
        double t = Double.MAX_VALUE;
        for (int i = 0; i < spheres.size(); i++) {
            double tstar = getSphereIntersection(eye, dir, spheres.get(i));
            if (t > tstar) {
                index = i;
                t = tstar;
            }
        }
        Vec3 intersection = new Vec3(line(eye, dir, t));
        return shade(intersection, index >= 0 ? spheres.get(index) : null);
    }

    private void raytrace() {
        int n = sqrtSamples;
        double xmin = engine.getXmin(), xmax = engine.getXmax();
        double ymin = engine.getYmin(), ymax = engine.getYmax();
        double dx = (xmax - xmin) / n;
        double dy = (ymax - ymin) / n;
        int upperBound = (int) ((1.0 / dx) * ((1.5 / camera.getRaw().getX()) - xmin));
        upperBound = Math.min(upperBound, n);
        int lowerBound = n - upperBound;
        double[] dir = new double[3];
        for (int i = lowerBound; i < upperBound; i++) {
            for (int j = lowerBound; j < upperBound; j++) {
                dir[0] = xmin + dx * i;
                dir[1] = ymin + dy * j;
                dir[2] = 1;
                Vector direction = new Vector(dir);
                direction = camera.getCamBasis().prodVector(direction);
                direction = Vector.normalize(direction);
                Vec3 color = trace(direction);
                int index = 3 * j + 3 * n * i;
                colorBuffer[index] = (int) (color.getX() * 255);
                colorBuffer[index + 1] = (int) (color.getY() * 255);
                colorBuffer[index + 2] = (int) (color.getZ() * 255);
            }
        }
    }

    @Override
    public void updateDraw() {
        engine.clearImageWithBackground();
        initColorBuffer(sqrtSamples);
        /**
         * render image
         * */
        Graphics image = engine.getImageGraphics();
        camera.update(dt);
        raytrace();
        int index;
        for (int i = 0; i < sqrtSamples; i++) {
            for (int j = 0; j < sqrtSamples; j++) {
                double x = (1.0 * widthChanged / sqrtSamples) * i;
                double y = (1.0 * heightChanged / sqrtSamples) * j;
                index = 3 * j + 3 * sqrtSamples * i;
                colorBuffer[index] = Math.max(0, Math.min(255, colorBuffer[index]));
                colorBuffer[index + 1] = Math.max(0, Math.min(255, colorBuffer[index + 1]));
                colorBuffer[index + 2] = Math.max(0, Math.min(255, colorBuffer[index + 2]));
                image.setColor(new Color(colorBuffer[index], colorBuffer[index + 1], colorBuffer[index + 2]));
                double w = Math.ceil((1.0 * widthChanged) / sqrtSamples);
                double h = Math.ceil((1.0 * heightChanged) / sqrtSamples);
                image.fillRect((int) x, (int) y, (int) w, (int) h);
            }
        }
        if (dt != 0.0) {
            Double aux = 1 / dt;
            NumberFormat format = new DecimalFormat("#,00");
            fps.setString(format.format(aux));
        }
        engine.drawElements();
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
        double dx = newMx - mx;
        double dy = newMy - my;
        Vec3 raw = camera.getRaw();
        Vec3 thrust = camera.getThrust();
        if (SwingUtilities.isLeftMouseButton(e)) {
            raw.setY(raw.getY() + 2 * Math.PI * (dx / widthChanged));
            raw.setZ(raw.getZ() + 2 * Math.PI * (dy / heightChanged));
            thrust.setY(2 * Math.PI * (dx / widthChanged));
            thrust.setZ(2 * Math.PI * (dy / heightChanged));
        } else {
            raw.setX(raw.getX() + (dx / widthChanged) + (dy / heightChanged));
            thrust.setX(((dx / widthChanged) + (dy / heightChanged)));
        }
        mx = newMx;
        my = newMy;
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    private interface SphereShader {
        /**
         * Get color.
         *
         * @param inter  the inter
         * @param sphere the sphere
         * @return the double [ ]
         */
        Vec3 getColor(Vec3 inter, Sphere sphere);
    }

    /**
     * The type Sphere.
     */
    private class Sphere {
        private Vec3 pos;
        private double radius;
        private int state;
        private SphereShader sphereShader;

        /**
         * Instantiates a new Sphere.
         *
         * @param pos    the pos
         * @param radius the radius
         * @param state  the state
         */
        public Sphere(Vec3 pos, double radius, int state) {
            this.pos = pos;
            this.radius = radius;
            this.state = state;
        }

        /**
         * Gets pos.
         *
         * @return the pos
         */
        public Vec3 getPos() {
            return pos;
        }

        /**
         * Sets pos.
         *
         * @param pos the pos
         */
        public void setPos(Vec3 pos) {
            this.pos = pos;
        }

        /**
         * Gets radius.
         *
         * @return the radius
         */
        public double getRadius() {
            return radius;
        }

        /**
         * Sets radius.
         *
         * @param radius the radius
         */
        public void setRadius(double radius) {
            this.radius = radius;
        }

        /**
         * Gets state.
         *
         * @return the state
         */
        public int getState() {
            return state;
        }

        /**
         * Sets state.
         *
         * @param state the state
         */
        public void setState(int state) {
            this.state = state;
        }

        /**
         * Gets sphere shader.
         *
         * @return the sphere shader
         */
        public SphereShader getSphereShader() {
            return sphereShader;
        }

        /**
         * Sets sphere shader.
         *
         * @param sphereShader the sphere shader
         */
        public void setSphereShader(SphereShader sphereShader) {
            this.sphereShader = sphereShader;
        }

        public Vec3 getNormal(Vec3 p) {
            Vec3 diff = Vec3.diff(p, pos);
            return Vec3.normalize(diff);
        }

        public Vec3 getColor(Vec3 inter) {
            return sphereShader.getColor(inter, this);
        }
    }

    private abstract class QuadraticFormShader implements SphereShader {

        @Override
        public Vec3 getColor(Vec3 inter, Sphere sphere) {
            double intensity = Vector.innerProd(inter, symMatrix.prodVector(inter));
            double lambdaMin = eigenValues[0];
            Double lambdaMax = eigenValues[2];
            intensity = (intensity - lambdaMin) / (lambdaMax - lambdaMin);
            return getRgbFromIntensity(intensity);
        }

        public abstract Vec3 getRgbFromIntensity(double intensity);
    }

    private class HeatShader extends QuadraticFormShader {

        @Override
        public Vec3 getRgbFromIntensity(double intensity) {
            double red = MyMath.clamp((10.0 / 4.0) * intensity, 0, 1);
            double green = MyMath.clamp((10.0 / 4.0) * (intensity - 0.4), 0, 1);
            double blue = MyMath.clamp(5 * (intensity - 0.8), 0, 1);
            return new Vec3(red, green, blue);
        }
    }


    private class DirectionShader implements SphereShader {

        @Override
        public Vec3 getColor(Vec3 inter, Sphere sphere) {
            double omega = 10;
            Vec3 n = Vec3.diff(inter, sphere.getPos());
            n = Vec3.normalize(n);

            Vec3 t = Vec3.matrixProd(symMatrix, n);
            t = Vec3.orthoProjection(t, n);
            t = Vec3.normalize(t);

            Vec3 b = Vec3.vectorProduct(n, t);
            b = Vec3.normalize(b);

            Camera3D camAux = new Camera3D();
            camAux.setRaw(new Vec3(1, Math.atan2(inter.getY(), inter.getX()), Math.asin(inter.getZ())));
            camAux.update(0);

            Vector grad1 = camAux.getInverseCamBasis().prodVector(t);
            grad1 = Vector.normalize(grad1);

            Vector grad2 = camAux.getInverseCamBasis().prodVector(b);
            grad2 = Vector.normalize(grad2);

            double theta = Math.atan2(grad1.getX(2), grad1.getX(1));
            double alpha = Math.sin(omega * theta);
            alpha = MyMath.clamp(alpha, -1, 1);
            alpha = (alpha + 1) / (2.0);
            HeatShader heatShader = new HeatShader();
            return heatShader.getRgbFromIntensity(alpha);
        }
    }

    private class SimpleShader implements SphereShader {

        @Override
        public Vec3 getColor(Vec3 inter, Sphere sphere) {
            return new Vec3(0.5, 0.5, 0.5);
        }
    }
}
