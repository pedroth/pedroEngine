package apps.src;

import algebra.src.Vec2;
import apps.utils.MyFrame;
import tools.simple.TextFrame;
import twoDimEngine.TwoDimEngine;
import twoDimEngine.elements.String2D;
import twoDimEngine.shaders.FillShader;
import twoDimEngine.shaders.PaintMethod2D;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;

public class ImplicitSurface extends MyFrame {
    protected double maxVision = 10.0;
    private TwoDimEngine engine;
    private String2D fps;
    private String helpTextStr = " w : move camera foward\n" + " s : move camera backward\n" + " a : move camera to left\n" + " d : move camera to right\n" + " q : move camera down\n" + " e : move camera up\n" + "\n" + " p : hd on/off\n" + "\n" + " drag mouse to change camera\n" + "\n" + "[0-9]: change surface";
    /**
     * mouse coordinates
     */
    private int mx, my, newMx, newMy;
    private int minSamples = 20000;
    private int maxSamples = 60000;
    private int samples = minSamples;
    private int sqrtSamples = (int) Math.floor(Math.sqrt(samples));
    private double[] eye = {4.0, 0.0, -2.0};
    private double[] dEye = {0.0, 0.0, 0.0};
    private double[] eyeThrust = {0.0, 0.0, 0.0};
    private double theta = 0;
    private double dTheta = 0;
    private double thetaThrust = 0;
    private double phi = 0;
    private double dPhi = 0;
    private double phiThrust = 0;
    private double[] xCam = {0.0, 1.0, 0.0};
    private double[] yCam = {0.0, 0.0, 1.0};
    private double[] zCam = {-1.0, 0.0, 0.0};
    private double[] surfaceColor = {0, 0, 255};
    private int statistics = 0;

    private int[] colorBuffer;

    private double[] lightColor = {255, 255, 255};

    private boolean isHD = false;

    private int indexFunction = 0;

    private int[] traceSamplesIndex = {10, 50, 100, 60, 90, 90, 60, 300, 20, 25};
    private int traceSamples = traceSamplesIndex[0];

    private double[][] particlePos = {{1, 0, 0}, {0, 0, 1}, {0, 1, 0}};
    private double[][] particleVel;

    public ImplicitSurface(String title, int width, int height) {
        super(title, width, height);
        engine = new TwoDimEngine(width, height);
        engine.setBackGroundColor(Color.blue);
        engine.setCamera(-1, 1, -1, 1);

        colorBuffer = new int[3 * samples];
        for (int i = 0; i < 3 * samples; i++) {
            colorBuffer[i] = 0;
        }
        PaintMethod2D painter = new FillShader(engine);
        fps = new String2D(new Vec2(-0.95, 0.83), "0");
        fps.setColor(Color.white);
        engine.addtoList(fps, painter);

        particleVel = new double[particlePos.length][particlePos[0].length];
        initParticles();
        init();
    }

    public static void main(String[] args) {
        new ImplicitSurface("Implicit Surface", 700, 700);
    }

    public void initParticles() {
        Random r = new Random();
        for (int i = 0; i < particlePos.length; i++) {
            particleVel[i][0] = -3 + 6 * r.nextDouble();
            particleVel[i][1] = -3 + 6 * r.nextDouble();
            particleVel[i][2] = -3 + 6 * r.nextDouble();
            particlePos[i][0] = -3 + 6 * r.nextDouble();
            particlePos[i][1] = -3 + 6 * r.nextDouble();
            particlePos[i][2] = -3 + 6 * r.nextDouble();
        }
    }

    double mySin(double x) {
        x = x % 4;
        double aux = (0.5 * x - 1);
        return 1 - aux * aux;
    }

    /**
     * f : R^3->R, is the function which the level set will be drawn. The level
     * set is f = 0.
     */
    double f(double x, double y, double z) {
        switch (indexFunction) {
            case 0:
                double r = (2 - Math.sqrt(x * x + y * y));
                return Math.sin(x) + Math.sin(y) - z;
            case 1:
                double rx = 0.70710678118 * x - 0.70710678118 * y;
                double ry = 0.70710678118 * x + 0.70710678118 * y;
                return Math.min((rx * rx * rx * rx * rx * rx * rx * rx * rx * rx + ry * ry * ry * ry * ry * ry * ry * ry * ry * ry + z * z * z * z * z * z * z * z * z * z), ((x - 1) * (x - 1) + (y - 1) * (y - 1) + (z - 1) * (z - 1))) - 1;
            case 2:
                r = x * x * (1 - x * x) - y * y;
                return r * r + z * z - 0.04;
            case 3:
                double c = 1;
                return ((x * x + y * y - c * c) * (x * x + y * y - c * c) + (z * z - 1) * (z * z - 1)) * ((y * y + z * z - c * c) * (y * y + z * z - c * c) + (x * x - 1) * (x * x - 1)) * ((z * z + x * x - c * c) * (z * z + x * x - c * c) + (y * y - 1) * (y * y - 1)) - 0.5;
            case 4:
                return 4 * x * x + 4 * y * y + 4 * z * z + 16 * x * y * z - 1;
            case 5:
                return x * x * x * x + z * z * z * z + y * y * y * y - x * x - y * y - z * z - x * x * y * y - x * x * z * z - y * y * z * z + 0.9;
            case 6:
                return x * x * x * x + z * z * z * z + y * y * y * y - (x * x + y * y + z * z) + 0.5;// 0.5*mySin(0.5*time);
            case 7:
                return (2.92 * (x - 1) * x * x * (x + 1) + 1.7 * y * y) * (2.92 * (x - 1) * x * x * (x + 1) + 1.7 * y * y) * (y * y - 0.88) * (y * y - 0.88) + (2.92 * (y - 1) * y * y * (y + 1) + 1.7 * z * z) * (2.92 * (y - 1) * y * y * (y + 1) + 1.7 * z * z) * (z * z - 0.88) * (z * z - 0.88) + (2.92 * (z - 1) * z * z * (z + 1) + 1.7 * x * x) * (2.92 * (z - 1) * z * z * (z + 1) + 1.7 * x * x) * (x * x - 0.88) * (x * x - 0.88) - 0.05;
            case 8:
                double[] aux = new double[particlePos.length];
                double acm = 0;
                for (int i = 0; i < particlePos.length; i++) {
                    aux[i] = (x - particlePos[i][0]) * (x - particlePos[i][0]) + (y - particlePos[i][1]) * (y - particlePos[i][1]) + (z - particlePos[i][2]) * (z - particlePos[i][2]);
                    acm += -(1 / (aux[i] + 1));
                }
                return acm + 0.9;
            case 9:
                return x * x - y * z;//x + y + z;
            default:
                return z;
        }
    }

    /**
     * 3D vectors
     */
    double[] add(double[] u, double[] v) {
        double[] ans = new double[3];
        ans[0] = u[0] + v[0];
        ans[1] = u[1] + v[1];
        ans[2] = u[2] + v[2];
        return ans;
    }

    double[] diff(double[] u, double[] v) {
        double[] ans = new double[3];
        ans[0] = u[0] - v[0];
        ans[1] = u[1] - v[1];
        ans[2] = u[2] - v[2];
        return ans;
    }

    double[] scalarMult(double s, double[] v) {
        double[] ans = new double[3];
        ans[0] = s * v[0];
        ans[1] = s * v[1];
        ans[2] = s * v[2];
        return ans;
    }

    double squaredNorm(double[] v) {
        return v[0] * v[0] + v[1] * v[1] + v[2] * v[2];
    }

    double myNorm(double[] v) {
        return Math.sqrt(squaredNorm(v));
    }

    double[] normalize(double[] v) {
        if (v[0] != 0.0 && v[1] != 0.0 && v[2] != 0.0) {
            return scalarMult(1 / myNorm(v), v);
        } else {
            return v;
        }
    }

    double innerProd(double[] u, double[] v) {
        return u[0] * v[0] + u[1] * v[1] + u[2] * v[2];
    }

    /**
     * return product between the matrix formed by (u,v,w) and x;
     */
    double[] matrixProd(double[] u, double[] v, double[] w, double[] x) {
        return add(add(scalarMult(x[0], u), scalarMult(x[1], v)), scalarMult(x[2], w));
    }

    /**
     * end vectors
     */

    void initColorBuffer(int numSamples) {
        samples = numSamples;
        sqrtSamples = (int) Math.floor(Math.sqrt(samples));
        colorBuffer = new int[3 * samples];
        for (int i = 0; i < 3 * samples; i++) {
            colorBuffer[i] = 0;
        }
    }

    void orbit(double dt) {
        dTheta = dTheta + dt * (thetaThrust - dTheta);
        theta = theta + dt * dTheta;
        theta = theta % (2 * Math.PI);

        dPhi = dPhi + dt * (phiThrust - dPhi);
        phi = phi + dt * dPhi;
        phi = phi % (2 * Math.PI);

        double cosP = Math.cos(phi);
        double cosT = Math.cos(theta);
        double sinP = Math.sin(phi);
        double sinT = Math.sin(theta);

        zCam[0] = -cosP * cosT;
        zCam[1] = -cosP * sinT;
        zCam[2] = -sinP;

        yCam[0] = -sinP * cosT;
        yCam[1] = -sinP * sinT;
        yCam[2] = cosP;

        xCam[0] = -sinT;
        xCam[1] = cosT;
        xCam[2] = 0;

        eyeThrust = matrixProd(xCam, yCam, zCam, eyeThrust);

        dEye = add(dEye, scalarMult(dt, diff(eyeThrust, dEye)));
        eye = add(eye, scalarMult(dt, dEye));
    }

    double intersectionVisionSphere(double[] init, double[] dir, double[] center, double radius) {
        double c = squaredNorm(diff(init, center)) - radius * radius;
        double b = 2 * innerProd(diff(init, center), dir);
        double a = squaredNorm(dir);
        double discr = b * b - 4 * a * c;

        if (discr < 0) {
            return 0;
        }

        double sol1 = (-b - Math.sqrt(discr)) / (2 * a);
        double sol2 = (-b + Math.sqrt(discr)) / (2 * a);

        return Math.min(Math.max(sol1, 0), Math.max(sol2, 0));
    }

    double[] line(double[] init, double[] dir, double t) {
        return add(init, scalarMult(t, dir));
    }

    double[] shading(double[] pos) {
        int shininess = 50;
        double[] normal = normalize(gradF(pos));
        double[] light = normalize(diff(eye, pos));
        double diffuse = Math.max(0, innerProd(normal, light));
        double specular = 1;
        for (int i = 0; i < shininess; i++) {
            specular *= diffuse;
        }
        double c = Math.max(Math.sin(20 * pos[2]), 0);
        double aux[] = {255, 255, 255};
        double ones[] = {1, 1, 1};
        ones = scalarMult(0.5, add(ones, normal));
        aux[2] = 1 * aux[0] * ones[0];
        aux[1] = 1 * aux[1] * ones[1];
        aux[0] = 1 * aux[2] * ones[2];
        return add(scalarMult(1 * diffuse, aux), scalarMult(0.5 * specular, lightColor));
    }

    /**
     * gradF: R^3->R^3 return the gradient of f at x.
     */
    double[] gradF(double[] x) {
        double[] ans = new double[3];
        double epsilon = 1E-6;
        ans[0] = (f(x[0] + epsilon, x[1], x[2]) - f(x[0], x[1], x[2])) / epsilon;
        ans[1] = (f(x[0], x[1] + epsilon, x[2]) - f(x[0], x[1], x[2])) / epsilon;
        ans[2] = (f(x[0], x[1], x[2] + epsilon) - f(x[0], x[1], x[2])) / epsilon;
        return ans;
    }

    double gradNormDiff(double[] init, double[] dir) {
        double epsilon = 1E-6;
        return (myNorm(gradF(line(init, dir, epsilon))) - myNorm(gradF(init))) / epsilon;
    }

    double[] trace(double[] init, double[] dir) {

        int maxIte = 10;
        double delta = maxVision / traceSamples;
        int index = -1;

        double[] ans = {0, 0, 0};
        double[] l = new double[3];
        l[0] = init[0];
        l[1] = init[1];
        l[2] = init[2];
        for (int i = 0; i < traceSamples; i++) {
            double[] nextL = add(l, scalarMult(delta, dir));
            statistics++;
            if (f(l[0], l[1], l[2]) * f(nextL[0], nextL[1], nextL[2]) < 0) {
                index = i;
                break;
            }
            l[0] = nextL[0];
            l[1] = nextL[1];
            l[2] = nextL[2];
        }
        statistics = 0;
        if (index == -1) {
            return ans;
        }
        double tLeft = delta * index;
        double tRight = delta * (index + 1);

        double root = 0;
        double fRoot = 1;

        for (int i = 0; i < maxIte; i++) {
            root = 0.5 * (tLeft + tRight);
            l = line(init, dir, root);
            fRoot = f(l[0], l[1], l[2]);
            if (Math.abs(fRoot) < 1E-3) {
                break;
            } else {
                l = line(init, dir, tLeft);
                if (Math.signum(f(l[0], l[1], l[2])) == Math.signum(fRoot)) {
                    tLeft = root;
                } else {
                    tRight = root;
                }
            }
        }
        double lambda = 0.1;
        return scalarMult(1 - fRoot / lambda, shading(l));
    }

    void raytrace() {
        int n = sqrtSamples;
        double xmin = engine.getXmin(), xmax = engine.getXmax();
        double ymin = engine.getYmin(), ymax = engine.getYmax();
        double[] dir = new double[3];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                dir[0] = xmin + ((xmax - xmin) / n) * i;
                dir[1] = ymin + ((ymax - ymin) / n) * j;
                dir[2] = 1;
                dir = normalize(dir);
                dir = matrixProd(xCam, yCam, zCam, dir);
                double[] color = trace(eye, dir);
                int index = 3 * j + 3 * n * i;
                colorBuffer[index] = (int) color[0];
                colorBuffer[index + 1] = (int) color[1];
                colorBuffer[index + 2] = (int) color[2];
            }
        }
    }

    public void euler() {
        double[][] rV = new double[particlePos.length][particlePos[0].length];
        for (int i = 0; i < rV.length; i++) {
            for (int j = 0; j < rV.length; j++) {
                if (i != j) {
                    rV[i] = add(rV[i], diff(particlePos[j], particlePos[i]));
                }
            }
        }
        double r = squaredNorm(rV[0]);
        double epsilon = Math.min(dt, 0.05);
        for (int i = 0; i < particlePos.length; i++) {
            rV[i] = scalarMult(1 / r, rV[i]);
            particleVel[i] = add(particleVel[i], scalarMult((epsilon), diff(rV[i], particleVel[i])));
            particlePos[i] = add(particlePos[i], scalarMult(epsilon, particleVel[i]));
        }
    }

    @Override
    public void reShape() {
        engine.setImageSize(widthChanged, heightChanged);
    }

    @Override
    public void updateDraw(Graphics g) {
        engine.clearImageWithBackground();
        /**
         * render image
         * */
        Graphics image = engine.getImageGraphics();
        orbit(dt);
        euler();
        raytrace();
        int index = 0;
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
        engine.paintImage(g);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        double[] ans = {0, 0, 0};
        double acc = 1;
        if (keyCode == KeyEvent.VK_W) {
            double[] aux = {0, 0, acc};
            ans = add(ans, aux);
        }
        if (keyCode == KeyEvent.VK_A) {
            double[] aux = {-acc, 0, 0};
            ans = add(ans, aux);
        }
        if (keyCode == KeyEvent.VK_S) {
            double[] aux = {0, 0, -acc};
            ans = add(ans, aux);
        }
        if (keyCode == KeyEvent.VK_D) {
            double[] aux = {acc, 0, 0};
            ans = add(ans, aux);
        }
        if (keyCode == KeyEvent.VK_Q) {
            double[] aux = {0, acc, 0};
            ans = add(ans, aux);
        }
        if (keyCode == KeyEvent.VK_E) {
            double[] aux = {0, -acc, 0};
            ans = add(ans, aux);
        }
        if (keyCode == KeyEvent.VK_0) {
            indexFunction = 0;
            traceSamples = traceSamplesIndex[indexFunction];
        }
        if (keyCode == KeyEvent.VK_1) {
            indexFunction = 1;
            traceSamples = traceSamplesIndex[indexFunction];
        }
        if (keyCode == KeyEvent.VK_2) {
            indexFunction = 2;
            traceSamples = traceSamplesIndex[indexFunction];
        }
        if (keyCode == KeyEvent.VK_3) {
            indexFunction = 3;
            traceSamples = traceSamplesIndex[indexFunction];
        }
        if (keyCode == KeyEvent.VK_4) {
            indexFunction = 4;
            traceSamples = traceSamplesIndex[indexFunction];
        }
        if (keyCode == KeyEvent.VK_5) {
            indexFunction = 5;
            traceSamples = traceSamplesIndex[indexFunction];
        }
        if (keyCode == KeyEvent.VK_6) {
            indexFunction = 6;
            traceSamples = traceSamplesIndex[indexFunction];
        }
        if (keyCode == KeyEvent.VK_7) {
            indexFunction = 7;
            traceSamples = traceSamplesIndex[indexFunction];
            eye[0] = 2;
            eye[1] = 0;
            eye[2] = 0;
        }
        if (keyCode == KeyEvent.VK_8) {
            indexFunction = 8;
            traceSamples = traceSamplesIndex[indexFunction];
            initParticles();
        }
        if (keyCode == KeyEvent.VK_9) {
            indexFunction = 9;
            traceSamples = traceSamplesIndex[indexFunction];
        }
        if (keyCode == KeyEvent.VK_P) {
            isHD = !isHD;
        }

        if (keyCode == KeyEvent.VK_H) {
            new TextFrame("help", helpTextStr);
        }

        if (isHD) {
            initColorBuffer(maxSamples);
        } else {
            initColorBuffer(minSamples);
        }

        eyeThrust = ans;
        // initColorBuffer(minSamples);

    }

    @Override
    public void keyReleased(KeyEvent e) {
        double[] aux = {0, 0, 0};
        eyeThrust = aux;
        // if(myNorm(dEye) < 1E-3){
        // initColorBuffer(maxSamples);
        // }

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent e) {
        mx = e.getX();
        my = e.getY();

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        thetaThrust = 0;
        phiThrust = 0;
        // initColorBuffer(maxSamples);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        newMx = e.getX();
        newMy = e.getY();
        double dx = newMx - mx;
        double dy = newMy - my;
        theta = theta - 2 * Math.PI * (dx / widthChanged);
        phi = phi - 2 * Math.PI * (dy / heightChanged);
        // thetaThrust = 40 * 2 * Math.PI * (dx / width);
        // phiThrust = 40 * 2 * Math.PI * (dy / height);
        mx = newMx;
        my = newMy;
        // initColorBuffer(minSamples);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub

    }

}
