package apps.src;

import algebra.src.Matrix;
import algebra.src.Vec2;
import algebra.src.Vec3;
import algebra.src.Vector;
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
    private static final int MIN_SAMPLES = 200;
    /*
     * max ray trace vision
     */
    private static final int MAX_SAMPLES = 400;
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

    private boolean isSmoothAnimation;

    private Sphere principalSphere;

    /**
     * Instantiates a new My frame.
     *
     * @param title  the title
     * @param width  the width
     * @param height the height
     */
    public EigenSimulation(String title, int width, int height, Matrix symMatrix, boolean isSmoothAnimation) {
        super(title, width, height);

        //init matrix
        this.symMatrix = symMatrix.getSubMatrix(1, 3, 1, 3);
        SymmetricEigen symmetricEigen = new SymmetricEigen(this.symMatrix);
        symmetricEigen.orderEigenValuesAndVector();
        eigenValues = symmetricEigen.getEigenValues();
        eigenVector = symmetricEigen.getEigenVectors();


        this.isSmoothAnimation = isSmoothAnimation;

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
        principalSphere = new Sphere(new Vec3(0, 0, 0), 1.0, new HeatShader(), null);
        spheres.add(principalSphere);


        //init camera
        this.camera = new Camera3D();
        this.camera.setRaw(new Vec3(3, 0, 0));
        this.camera.update(0);
        //init Image
        initColorBuffer(MIN_SAMPLES);

        //display frame
        this.init();
    }

    public static void main(String[] args) {
        Matrix symMatrix = new Matrix(3, 3);
        symMatrix.fillRandom(-10, 10);
        symMatrix = Matrix.add(symMatrix, Matrix.transpose(symMatrix));
        Matrix diagonal = Matrix.diag(new Vec3(0, 0.5, 4));
        Matrix symMatrix1 = new Matrix(new double[][]{{1, 1, 1}, {1, 1, 1}, {1, 1, 1}});
        EigenSimulation eigenSimulation = new EigenSimulation("Eigen Simulation", 500, 500, symMatrix, true);
        Sphere sphere1 = eigenSimulation.new Sphere(new Vec3(1, 0, 0), 0.1, eigenSimulation.new SimpleShader(new Vec3(0.1, 0.1, 0.9)), eigenSimulation.new Instrisic());
        Sphere sphere2 = eigenSimulation.new Sphere(new Vec3(1, 0, 0), 0.1, eigenSimulation.new SimpleShader(new Vec3(0.1, 0.9, 0.1)), eigenSimulation.new PowerMethod());
        eigenSimulation.addSphereEigenUpdate(sphere1);
        eigenSimulation.addSphereEigenUpdate(sphere2);
    }

    public void addSphereEigenUpdate(Sphere s) {
        spheres.add(s);
    }

    public void setSphereShader(SphereShader sphereShader) {
        principalSphere.setSphereShader(sphereShader);
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

        double sqrt = Math.sqrt(discr);
        double sol1 = (-b - sqrt) / (2 * a);
        double sol2 = (-b + sqrt) / (2 * a);
        double m = sol1 * sol2;
        if (m < 0) {
            return Math.max(sol1, sol2);
        } else if (m == 0) {
            return 0;
        } else {
            return sol1 > 0 && sol2 > 0 ? Math.min(sol1, sol2) : Double.MAX_VALUE;
        }
    }

    private double getSphereIntersection2(Vector eye, Vector dir, Sphere sphere) {
        int maxIte = 10;
        double h = maxVision / (maxIte);
        Vec3 pos = sphere.getPos();
        double radius = sphere.getRadius();
        double t;
        for (t = 0; t < maxVision; ) {
            Vector p = line(eye, dir, t);
            if (Vector.diff(pos, p).norm() < radius) {
                return t;
            }
            t += h;
        }
        if (t >= maxVision) {
            return Double.MAX_VALUE;
        }

        double tLeft = t - h;
        double tRight = t;

        double root = 0;
        double fRoot = 1;
        Vector l = new Vector(3);
        for (int i = 0; i < 10; i++) {
            root = 0.5 * (tLeft + tRight);
            l = line(eye, dir, root);
            fRoot = Vector.diff(pos, l).norm() - radius;
            if (Math.abs(fRoot) < 1E-3) {
                break;
            } else {
                l = line(eye, dir, tLeft);
                if (Math.signum(Vector.diff(pos, l).norm() - radius) == Math.signum(fRoot)) {
                    tLeft = root;
                } else {
                    tRight = root;
                }
            }
        }
        return root;
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
        eigenUpdate(dt);
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

    private void eigenUpdate(double dt) {
        for (int i = 1; i < spheres.size(); i++) {
            Sphere sphere = spheres.get(i);
            Vec3 newEigen = sphere.update();
            if (isSmoothAnimation) {
                Vec3 pos = sphere.getPos();
                Vec3 diff = Vec3.diff(newEigen, pos);
                diff = Vec3.orthoProjection(diff, pos);
                newEigen = Vec3.add(pos, Vec3.scalarProd(dt, diff));
            }
            sphere.setPos(newEigen);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {


    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_1:
                this.setSphereShader(new HeatShader());
                break;
            case KeyEvent.VK_2:
                this.setSphereShader(new DirectionShader());
                break;
            case KeyEvent.VK_3:
                this.setSphereShader(new Lic());
                break;
            default:
                Vec3 randomV = new Vec3();
                randomV.fillRandom(-1, 1);
                randomV = Vec3.normalize(randomV);
                for (int i = 1; i < spheres.size(); i++) {
                    spheres.get(i).setPos(randomV);
                }
                break;
        }
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

    private interface EigenUpdate {
        Vector update(Matrix symMatrix, Vector init);
    }


    /**
     * The type Sphere.
     */
    private class Sphere {

        private Vec3 pos;
        private double radius;
        private SphereShader sphereShader;
        private EigenUpdate algo;

        public Sphere(Vec3 pos, double radius, SphereShader sphereShader, EigenUpdate algo) {
            this.pos = pos;
            this.radius = radius;
            this.sphereShader = sphereShader;
            this.algo = algo;
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

        public EigenUpdate getAlgo() {
            return algo;
        }

        public void setAlgo(EigenUpdate algo) {
            this.algo = algo;
        }

        public Vec3 update() {
            return new Vec3(algo.update(symMatrix, this.getPos()));
        }
    }

    //Shaders

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
            double omega = 10.0;

            Vec2 interSphereCoord = new Vec2(Math.atan2(inter.getY(), inter.getX()), Math.asin(inter.getZ()));
            //Kind of Jacobian
            Camera3D myCam = new Camera3D();
            myCam.setRaw(new Vec3(1, interSphereCoord.getX(), interSphereCoord.getY()));
            myCam.update(0);
            Matrix camBasis = myCam.getInverseCamBasis();
            // vector field
            Vector grad = symMatrix.prodVector(inter);
            // vector field in sphere coordinates
            Vector gradSphereCoord = camBasis.prodVector(grad);
            Vec2 gradSphere2 = new Vec2(gradSphereCoord.getX(1), gradSphereCoord.getX(2));

            //function computation
            double alpha = Math.sin(omega * gradSphere2.getAngle()) * Math.sin((omega / eigenValues[2]) * gradSphere2.norm());
            alpha = MyMath.clamp(alpha, -1, 1);
            alpha = (alpha + 1) / (2.0);
            HeatShader heatShader = new HeatShader();
            return heatShader.getRgbFromIntensity(alpha);
        }
    }

    private class Lic implements SphereShader {
        int n = 200;
        int nn = n * n;
        double[] texOrig = new double[nn];
        Vec2[] vecField = new Vec2[nn];
        Vec2[] intDirField = new Vec2[nn];
        double[] tex = new double[nn];
        int kernelSize = 15;

        public Lic() {
            for (int i = 0; i < nn; i++) {
                texOrig[i] = Math.random();
                double x = -Math.PI + ((i % n) * (2 * Math.PI)) / (n - 1);
                double y = Math.PI - ((i / n) * (2 * Math.PI)) / (n - 1);
                Camera3D cam = new Camera3D();
                cam.setRaw(new Vec3(1.0, x, y));
                cam.update(0);
                Vec3 grad = new Vec3(cam.getInverseCamBasis().prodVector(symMatrix.prodVector(cam.getEye())));
                vecField[i] = new Vec2(grad.getX(), grad.getY());
                int[] v1 = sphereCoor2Int(new Vec2(grad.getX(), grad.getY()));
                int[] v2 = sphereCoor2Int(new Vec2(0, 0));
                int[] adjV = getAdjTex(new int[]{0, 0}, new int[]{v1[0] - v2[0], v1[1] - v2[0]});
                intDirField[i] = new Vec2(adjV[0], adjV[1]);
            }

            for (int i = 0; i < nn; i++) {
                double x = -Math.PI + ((i % n) * (2 * Math.PI)) / (n - 1);
                double y = Math.PI - ((i / n) * (2 * Math.PI)) / (n - 1);
                tex[i] = getTexOrig(new Vec2(x, y), kernelSize);
            }
        }

        @Override
        public Vec3 getColor(Vec3 inter, Sphere sphere) {
            inter = Vec3.diff(inter, sphere.getPos());
            Vec2 interSphereCoord = new Vec2(Math.atan2(inter.getY(), inter.getX()), Math.asin(inter.getZ()));
            Vec2 x = sphereCoor2IntDouble(interSphereCoord);
            int[] xfloor = sphereCoor2Int(interSphereCoord);
            double f11 = getTex(xfloor);
            double f21 = getTex(new int[]{(xfloor[0] + 1) % n, xfloor[1]});
            double f12 = getTex(new int[]{xfloor[0], (xfloor[1] + 1) % n});
            double f22 = getTex(new int[]{(xfloor[0] + 1) % n, (xfloor[1] + 1) % n});
            double f1 = f11 + (f21 - f11) * (x.getX() - xfloor[0]);
            double f2 = f12 + (f22 - f12) * (x.getX() - xfloor[0]);
            double f = f1 + (f2 - f1) * (x.getY() - xfloor[1]);
            return new Vec3(f, f, f);
        }

        private double getTexOrig(Vec2 interSphereCoord, int steps) {
            double acc = 0;
            int[] i = sphereCoor2Int(interSphereCoord);
            int[] copyI = {i[0], i[1]};
            acc += getTexOrig(i);
            for (int j = 0; j < steps / 2; j++) {
                i = getNextTex(i, 1);
                acc += getTexOrig(i);
            }
            i[0] = copyI[0];
            i[1] = copyI[1];
            for (int j = 0; j < steps / 2; j++) {
                i = getNextTex(i, -1);
                acc += getTexOrig(i);
            }
            acc /= steps;
            return acc;
        }

        private int[] getNextTex(int[] i, int dir) {
            Vec2 v = getVecTex(i);
            Vec2 p = Vec2.add(new Vec2(i[0], i[1]), Vec2.scalarProd(dir, v));
            return new int[]{MyMath.positiveMod((int) p.getX(), n), MyMath.positiveMod((int) p.getY(), n)};
        }

        private int[] getAdjTex(int[] init, int[] next) {
            int[] k = {-1, 0, 1};
            int length = k.length;
            int lenlen = length * length;

            int[] diffVec = new int[]{next[0] - init[0], next[1] - init[1]};

            Vec2 diffAux = new Vec2(diffVec[0], diffVec[1]);
            diffAux = Vec2.normalize(diffAux);

            double max = Double.MIN_VALUE;
            int maxIndex = -1;

            for (int index = 0; index < lenlen; index++) {
                Vec2 dir = new Vec2(k[index % length], k[index / length]);
                dir = Vec2.normalize(dir);
                double dot = Vec2.innerProd(diffAux, dir);
                if (max < dot) {
                    max = dot;
                    maxIndex = index;
                }
            }

            if (maxIndex == -1) {
                return init;
            }
            return new int[]{init[0] + k[maxIndex % length], init[1] + k[maxIndex / length]};
        }

        double getTex(int[] i) {
            i[0] = MyMath.positiveMod(i[0], n);
            i[1] = MyMath.positiveMod(i[1], n);
            return tex[i[0] + n * i[1]];
        }

        double getTexOrig(int[] i) {
            i[0] = MyMath.positiveMod(i[0], n);
            i[1] = MyMath.positiveMod(i[1], n);
            return texOrig[i[0] + n * i[1]];
        }

        Vec2 getVecTex(int[] i) {
            i[0] = MyMath.positiveMod(i[0], n);
            i[1] = MyMath.positiveMod(i[1], n);
            return intDirField[i[0] + n * i[1]];
        }

        int[] sphereCoor2Int(Vec2 coord) {
            return new int[]{(int) Math.floor(getIntCoordinate(coord.getX(), -Math.PI, Math.PI, n)), (int) Math.floor(getIntCoordinate(coord.getY(), Math.PI, -Math.PI, n))};
        }

        Vec2 sphereCoor2IntDouble(Vec2 coord) {
            return new Vec2(getIntCoordinate(coord.getX(), -Math.PI, Math.PI, n), getIntCoordinate(coord.getY(), Math.PI, -Math.PI, n));
        }

        double getIntCoordinate(double x, double xmin, double xmax, int samples) {
            return ((x - xmin) * ((samples - 1) / (xmax - xmin)));
        }
    }

    private class SimpleShader implements SphereShader {
        private Vec3 rgb;

        public SimpleShader(Vec3 rgb) {
            this.rgb = rgb;
        }

        @Override
        public Vec3 getColor(Vec3 inter, Sphere sphere) {
            return rgb;
        }
    }


    // Eigen Updaters

    private class PowerMethod implements EigenUpdate {

        @Override
        public Vector update(Matrix symMatrix, Vector init) {
            init = Vector.normalize(init);
            Vector vector = symMatrix.prodVector(init);
            return Vector.normalize(vector);
        }
    }

    private class Instrisic implements EigenUpdate {

        private Vector computeV(Vector u) {
            Vector ans = symMatrix.prodVector(u);
            ans = Vector.orthoProjection(ans, u);
            return Vector.normalize(ans);
        }

        private Matrix computeS(Vector u, Vector v) {
            double uSu = Vector.innerProd(u, symMatrix.prodVector(u));
            double uSv = Vector.innerProd(u, symMatrix.prodVector(v));
            double vSv = Vector.innerProd(v, symMatrix.prodVector(v));
            return new Matrix(new double[][]{{uSu, uSv}, {uSv, vSv}});
        }

        private double f(Matrix S, double t) {
            Vec2 alpha = new Vec2(Math.cos(t), Math.sin(t));
            return Vector.innerProd(alpha, symMatrix.prodVector(alpha));
        }

        private double df(Matrix S, double t) {
            Vec2 alpha = new Vec2(Math.cos(t), Math.sin(t));
            Matrix R = new Matrix(new double[][]{{0, -1}, {1, 0}});
            return 2 * Vector.innerProd(S.prodVector(alpha), R.prodVector(alpha));
        }

        private double d2f(Matrix S, double t) {
            Vec2 alpha = new Vec2(Math.cos(t), Math.sin(t));
            Matrix R = new Matrix(new double[][]{{0, -1}, {1, 0}});
            R = Matrix.prod(R, R);
            return 4 * Vector.innerProd(S.prodVector(alpha), R.prodVector(alpha));
        }

        private Vector gamma(Vector u, Vector v, double t) {
            return Vector.add(Vector.scalarProd(Math.cos(t), u), Vector.scalarProd(Math.sin(t), v));
        }

        @Override
        public Vector update(Matrix symMatrix, Vector init) {
            Vector u = Vector.normalize(init);
            Vector v = computeV(u);
            Matrix S = computeS(u, v);
            double df;
            double t = 0;
            do {
                df = df(S, t);
                double d2f = d2f(S, t);
                t = t - df / d2f;
            } while (Math.abs(df) > 1E-3);
            return Vector.normalize(gamma(u, v, t));
        }

    }
}