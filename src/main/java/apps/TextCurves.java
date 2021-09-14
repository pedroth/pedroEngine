package apps;

import algebra.Vec2;
import algebra.Vec3;
import algebra.Vector;
import apps.utils.MyFrame;
import apps.utils.TextFrame;
import nlp.lowbow.simpleLowBow.*;
import nlp.symbolSampler.SymbolSampler;
import nlp.symbolSampler.TopKSymbol;
import nlp.textSplitter.SpaceSplitter;
import numeric.Camera3D;
import numeric.MyMath;
import twoDimEngine.TwoDimEngine;
import twoDimEngine.elements.Line2D;
import twoDimEngine.elements.String2D;
import twoDimEngine.shaders.PaintMethod2D;
import twoDimEngine.shaders.ThickLineShader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pedro
 */
public class TextCurves extends MyFrame implements MouseWheelListener {
    private static final TextFrame HELP_FRAME = TextFrame.builder()
            .addLine("<left mouse> to change camera / move camera")
            .addLine("<right mouse> zoom in/out (pca mode)")
            .buildWithTitle("Help");
    private TwoDimEngine engine;
    private PaintMethod2D shader;
    /**
     * mouse coordinates
     */
    private int mx, my, newMx, newMy;
    /**
     * User Interface crap
     */
    private TextArea inOut;
    private Button button;
    private JFrame input;
    private TextField sigmaText;
    private JSlider samplesSlider;
    private Label samplesSliderTxt;
    private double samplesIn;
    private double samplesInMin;
    private double samplesInMax;
    private Checkbox txtVisibleCheckBox;
    private Label frameState;
    private Button simplexOnPolyButton;
    private JSlider heatLambdaSlider;
    private double lambdaIn;
    private double lambdaInMin;
    private double lambdaInMax;
    private Label lambdaSliderTxt;
    private Choice flowSolverChoice;
    /**
     * locally weighted bag of wordsIndex
     */
    private LowBowManagerPca lowbowM;
    /**
     * read a text
     */
    private boolean isReady;
    /**
     * processed the text: lowbow representation and pca computation done
     */
    private boolean isProcess;
    /**
     * Camera
     */
    private Camera3D camera;
    /**
     * pca curve projected in the plane
     */
    private ArrayList<Vec2[]> projCurves;
    private ArrayList<Line2D[]> projLines;
    /**
     * pca curve statistics
     */
    private Vec3 centerMass;
    private double stdev;
    /**
     * text curve
     */
    private ArrayList<String2D[]> strCurves;
    /**
     * state that determines when to draw simplex on polygon
     * state = 0 : pca
     * state = 1 : drawBarycentric
     * state = 2 : dont draw
     */
    private int simplexOnPolyState = 1;
    /**
     * polygon where the curve will be drawn
     */
    private Vec2[] polygon;

    private HeatMethod heatMethod = new MatrixHeatFlow();

    private SymbolSampler symbolSampler = new TopKSymbol(1);

    enum FlowsEnum {
        SPARSE("Sparse", new SparseHeatFlow()),
        MATRIX("Matrix", new MatrixHeatFlow()),
        KERNEL("Kernel", new HeatKernelFlow()),
        GRAPH_LAPLACIAN("GraphLaplacian", new GraphLaplacianFlow()),
        HEAT_EXPONENTIAL("HeatExponential", new HeatFlow()),
        EIGEN_HEAT("EigenHeat", new LaplacianEigenFlow());

        private HeatMethod heatFlow;
        private String flowName;
        private static Map<String, FlowsEnum> flowsByNameMap = new HashMap<>(FlowsEnum.values().length);

        static {
            for (FlowsEnum flowsEnum : FlowsEnum.values()) {
                flowsByNameMap.put(flowsEnum.flowName, flowsEnum);
            }
        }

        FlowsEnum(String flowName, HeatMethod heatFlow) {
            this.flowName = flowName;
            this.heatFlow = heatFlow;
        }

        public static HeatMethod getHeatMethodFromName(String name) {
            return flowsByNameMap.get(name).heatFlow;
        }
    }


    public TextCurves(String title, int width, int height) {
        super(title, width, height);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        engine = new TwoDimEngine(width, height);
        shader = new ThickLineShader(engine);
        engine.setBackGroundColor(Color.white);
        engine.setCamera(-1, 1, -1, 1);
        /**
         * GUI crap
         */
        input = new JFrame("Input text File");
        input.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        input.setLayout(new GridLayout(1, 2));
        Panel p1 = new Panel(new GridLayout(7, 1));
        input.setResizable(false);
        input.setSize((int) Math.floor(0.6 * width), (int) Math.floor(0.3 * width));
        input.setLocation(width, 0);
        /**
         * Text area
         */
        inOut = new TextArea();
        inOut.setText("Hello , please input some text here.\n For example a c c c b b a c c");
        input.add(inOut);
        /**
         * buttons
         */
        Panel pbuttons = new Panel(new GridLayout(1, 2));
        button = new Button("Load");
        button.addActionListener(new myActionListener(this));
        Button addButton = new Button("Reset");
        addButton.addActionListener(e -> reset());
        pbuttons.add(button);
        pbuttons.add(addButton);
        p1.add(pbuttons);
        /**
         * samples text
         */
        Panel p2 = new Panel(new GridLayout(1, 2));
        samplesInMin = 1.0;
        samplesInMax = 10.0;
        samplesIn = samplesInMin;
        samplesSliderTxt = new Label("samples/txtLength:" + samplesIn);
        p2.add(samplesSliderTxt);
        samplesSlider = new JSlider(0, 9, 0);
        samplesSlider.addChangeListener(e -> {
            JSlider aux = (JSlider) e.getSource();
            samplesIn = samplesInMin + ((samplesInMax - samplesInMin) / (aux.getMaximum())) * (aux.getValue());
            samplesSliderTxt.setText("samples/txtLength:" + samplesIn);
        });
        p2.add(samplesSlider);
        p1.add(p2);
        /**
         * sigma text
         */

        Panel p3 = new Panel(new GridLayout(1, 2));
        p3.add(new Label("Sigma[> 0]: "));
        sigmaText = new TextField();
        sigmaText.setText("auto");
        p3.add(sigmaText);
        p1.add(p3);
        /**
         * check box
         */
        Panel p4 = new Panel(new GridLayout(1, 2));
        frameState = new Label("");
        p4.add(frameState);
        txtVisibleCheckBox = new Checkbox("Text visible");
        p4.add(txtVisibleCheckBox);
        p1.add(p4);
        /**
         * generate text
         */
        Panel p5 = new Panel(new GridLayout(1, 2));
        Button generateButton = new Button("Generate");
        generateButton.addActionListener(e -> generateText());
        /**
         * Poly AbstractSimplex
         */
        simplexOnPolyButton = new Button("PCA");
        simplexOnPolyButton.addActionListener(e -> {
            if (simplexOnPolyButton.getLabel().equals("PCA")) {
                simplexOnPolyState = 0;
                buildPca(true);
            } else {
                simplexOnPolyState = 1;
            }

            if (simplexOnPolyState == 1 || simplexOnPolyState == 2) {
                simplexOnPolyButton.setLabel("PCA");
                engine.setCamera(-1, 1, -1, 1);
            } else {
                simplexOnPolyButton.setLabel("Barycentric");
            }
        });
        p5.add(simplexOnPolyButton);
        p5.add(generateButton);
        p1.add(p5);

        /**
         * heat flow button
         */
        Panel p6 = new Panel(new GridLayout(1, 3));
        lambdaInMin = 0.0001;
        lambdaInMax = 0.9999;
        lambdaIn = lambdaInMin;
        lambdaSliderTxt = new Label("" + lambdaIn);
        heatLambdaSlider = new JSlider(0, 99, 0);
        heatLambdaSlider.addChangeListener(e -> {
            JSlider aux = (JSlider) e.getSource();
            lambdaIn = lambdaInMin + ((lambdaInMax - lambdaInMin) / (aux.getMaximum())) * (aux.getValue());
            lambdaSliderTxt.setText("" + lambdaIn);
        });
        Button heatFlowButton = new Button("Heat Flow");
        heatFlowButton.addActionListener(e -> {
            HeatMethod heat = heatMethod;
            heatFlow(lambdaIn, heat);
        });
        p6.add(lambdaSliderTxt);
        p6.add(heatLambdaSlider);
        p6.add(heatFlowButton);
        p1.add(p6);

        Panel p7 = new Panel(new GridLayout(1, 3));
        p7.add(new Label(""));
        p7.add(new Label(""));
        flowSolverChoice = new Choice();
        for (FlowsEnum flowsEnum : FlowsEnum.values()) {
            flowSolverChoice.add(flowsEnum.name());
        }

        flowSolverChoice.addItemListener(e -> this.heatMethod = FlowsEnum.getHeatMethodFromName(flowSolverChoice.getSelectedItem()));
        flowSolverChoice.select("Matrix");
        p7.add(flowSolverChoice);
        p1.add(p7);

        input.add(p1);

        isReady = false;
        isProcess = false;
        input.setVisible(true);
        camera = new Camera3D();
        projCurves = new ArrayList<>();
        projLines = new ArrayList<>();
        strCurves = new ArrayList<>();
        lowbowM = new LowBowManagerPca();
        centerMass = new Vec3();
        stdev = 0;

        this.addMouseWheelListener(this);
    }

    public static void main(String[] args) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        new TextCurves("Teste", (int) (screenSize.getWidth() * 0.60), (int) (screenSize.getHeight() * 0.75));
    }

    private void buildPca(boolean isUpdate) {
        frameState.setText("build pca");
        lowbowM.buildPca();
        if (isUpdate) {
            updateCurveStats();
        }
        frameState.setText("");
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyPressed(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_H:
                HELP_FRAME.setVisible(true);
                break;
            default:
                break;
        }

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
        Vec3 thrust = camera.getThrust();
        thrust.setX(0);
        thrust.setY(0);
        thrust.setZ(0);
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
        Vec3 raw = camera.getRaw();
        Vec3 thrust = camera.getThrust();
        double power = 40 * stdev;
        if (SwingUtilities.isLeftMouseButton(e)) {
            raw.setY(raw.getY() + 2 * Math.PI * (dx / widthChanged));
            raw.setZ(raw.getZ() + 2 * Math.PI * (dy / heightChanged));
            thrust.setY(power * 2 * Math.PI * (dx / widthChanged));
            thrust.setZ(power * 2 * Math.PI * (dy / heightChanged));
        } else {
            raw.setX(raw.getX() + (dx / widthChanged) + (dy / heightChanged));
            thrust.setX(power * ((dx / widthChanged) + (dy / heightChanged)));
        }

        if (simplexOnPolyState > 0) {
            double h = -2 * (dx / widthChanged);
            double k = 2 * (dy / heightChanged);
            engine.setCamera(engine.getXmin() + h, engine.getXmax() + h, engine.getYmin() + k, engine.getYmax() + k);
        }

        mx = newMx;
        my = newMy;

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void reShape() {
        engine.setImageSize(widthChanged, heightChanged);
    }

    /**
     * update projected curve
     */
    private void updateCurvePca() {
        /**
         * distance to drawing plane
         */
        double d = 1;
        ArrayList<LowBowPca> l = lowbowM.getDocModels();
        int n = l.size();

        for (int i = 0; i < n; i++) {
            Vec3[] pcaCurve = l.get(i).getPcaCurve();
            Vec2[] projCurve = projCurves.get(i);
            Line2D[] projLine = projLines.get(i);
            /**
             * Projective projection
             */
            for (int j = 0; j < pcaCurve.length; j++) {
                Vec3 u = new Vec3(pcaCurve[j].getX(), pcaCurve[j].getY(), pcaCurve[j].getZ());
                Vec3 v = Vec3.diff(u, camera.getEye());
                v = Vec3.matrixProd(camera.getInverseCamBasis(), v);
                int boundj = Math.min(j, projLine.length - 1);
                if (v.getZ() < (d / 1000)) {
                    projLine[boundj].setVisible(false);
                } else {
                    projLine[boundj].setVisible(true);
                    projCurve[j].setX(v.getX() * (d / v.getZ()));
                    projCurve[j].setY(v.getY() * (d / v.getZ()));
                }
            }
        }
    }

    private void updateCurveOnPoly() {
        ArrayList<LowBowPca> l = lowbowM.getDocModels();
        int n = l.size();
        int wordL = l.get(0).getNumWords();
        if ((polygon == null) || (polygon.length != wordL)) {
            buildPoly(wordL);
        }

        for (int i = 0; i < n; i++) {
            Vector[] curve = l.get(i).getCurve();
            Vec2[] projCurve = projCurves.get(i);
            Line2D[] projLine = projLines.get(i);
            /**
             * Barycentric coordinates to world coordinates calculation
             */
            for (int j = 0; j < projCurve.length; j++) {
                Vec2 acm = new Vec2();
                for (int k = 0; k < wordL; k++) {
                    acm = Vec2.add(acm, Vec2.scalarProd(curve[j].getX(k + 1), polygon[k]));
                }
                int boundj = Math.min(j, projLine.length - 1);
                projLine[boundj].setVisible(true);
                projCurve[j].setX(acm.getX());
                projCurve[j].setY(acm.getY());
            }
        }
        simplexOnPolyState = 2;
    }

    private void buildPoly(int wordL) {
        polygon = new Vec2[wordL];
        double t = 0;
        double step = (2 * Math.PI) / wordL;
        for (int i = 0; i < wordL; i++) {
            polygon[i] = new Vec2(Math.cos(t), Math.sin(t));
            t += step;
        }
    }

    private void updateStringCurve() {
        ArrayList<LowBowPca> l = lowbowM.getDocModels();
        int n = l.size();
        for (int i = 0; i < n; i++) {
            String2D[] strCurve = strCurves.get(i);
            LowBow lowbow = l.get(i);
            Vec2[] projCurve = projCurves.get(i);
            Line2D[] projLine = projLines.get(i);
            for (int j = 0; j < strCurve.length; j++) {
                int tLength = lowbow.getTextLength();
                int samples = lowbow.getSamples();
                double s = (samples - 1.0) / (tLength - 1.0);
                int k = (int) Math.floor(MyMath.clamp(s * j, 0, samples - 1));
                if (txtVisibleCheckBox.getState() && projLine[i].isVisible()) {
                    strCurve[j].setVisible(true);
                    strCurve[j].setVertex(projCurve[k], 0);
                    strCurve[j].setString(symbolSampler.nextSymbol(lowbow.getCurve()[k], lowbow.getSimplex()));
                } else {
                    strCurve[j].setVisible(false);
                }
            }
        }
    }

    /**
     * update pca curve statistics and update camera.
     */
    private void updateCurveStats() {
        ArrayList<LowBowPca> lowbows = lowbowM.getDocModels();
        LowBowPca lowbow = lowbows.get(lowbows.size() - 1);
        int n = lowbow.getPcaCurve().length;
        Vec3[] pcaCurve = lowbow.getPcaCurve();
        for (int i = 0; i < n; i++) {
            centerMass = Vec3.add(centerMass, pcaCurve[i]);
        }
        centerMass = Vec3.scalarProd(1.0 / n, centerMass);
        double stdevAcm = 0;
        for (int i = 0; i < n; i++) {
            Vec3 aux = Vec3.diff(pcaCurve[i], centerMass);
            stdevAcm += aux.squareNorm();
        }
        stdev = Math.max(Math.sqrt(stdevAcm), stdev);
        camera.setFocalPoint(centerMass);

        Vec3 raw = camera.getRaw();
        double percent = 1.10;
        camera.setRaw(new Vec3(percent * stdev, raw.getY(), raw.getZ()));
    }

    @Override
    public void updateDraw() {
        engine.clearImageWithBackground();
        camera.update(dt);
        if (simplexOnPolyState == 1) {
            updateCurveOnPoly();
        } else if (simplexOnPolyState == 0) {
            updateCurvePca();
        } else {
        }
        updateStringCurve();
        engine.drawElements();
        this.setTitle("Fps : " + this.getFps());
        drawImage(engine.getImage());
    }

    public void reset() {
        this.setVisible(false);
        lowbowM.removeAll();
        engine.removeAllElements();
        projCurves.removeAll(projCurves);
        projLines.removeAll(projLines);
        strCurves.removeAll(strCurves);
        centerMass = new Vec3();
        stdev = 0;
    }

    public void addCurveToEngine(LowBow lowbow) {
        Color color = Color.getHSBColor((float) Math.random(), 1.0f, 1.0f);
        int randSamples = (int) Math.round(Math.random() * 5);

        for (int i = 0; i < randSamples; i++) {
            color = Color.getHSBColor((float) Math.random(), 1.0f, 1.0f);
        }

        int n = lowbow.getSamples();

        Vec2[] projCurve = new Vec2[n];
        Line2D[] projLine = new Line2D[n - 1];
        for (int i = 0; i < n - 1; i++) {
            projCurve[i] = (projCurve[i] == null) ? new Vec2() : projCurve[i];
            projCurve[i + 1] = new Vec2();
            projLine[i] = new Line2D(projCurve[i], projCurve[i + 1]);
            projLine[i].setColor(color);
            engine.addtoList(projLine[i], shader);
        }

        projLines.add(projLine);
        projCurves.add(projCurve);

        String[] text = lowbow.getText();
        String2D[] strCurve = new String2D[text.length];

        for (int i = 0; i < text.length; i++) {
            strCurve[i] = new String2D(new Vec2(), text[i]);
            strCurve[i].setColor(Color.blue);
            strCurve[i].setVisible(false);
            engine.addtoList(strCurve[i]);
        }

        strCurves.add(strCurve);
    }

    public void generateText() {
        ArrayList<LowBowPca> lowList = lowbowM.getDocModels();

        if (lowList == null)
            return;

        LowBow low = lowList.get(lowList.size() - 1);
        new TextFrame("Generated Text", low.generateText(symbolSampler));
    }

    public void pcaCoords() {
        ArrayList<LowBowPca> low = lowbowM.getDocModels();
        LowBowPca curve = low.get(low.size() - 1);
        Vec3[] pca = curve.getPcaCurve();
        String[] text = curve.getText();
        for (int i = 0; i < text.length; i++) {
            String string = text[i];
            int k = (int) Math.floor(MyMath.clamp(curve.getSamplesPerTextLength() * i, 0, curve.getSamples()));
            System.out.println(string + "\t" + pca[k].getX() + "\t" + pca[k].getY() + "\t" + pca[k].getZ());
        }
    }

    public void heatFlow(double lambda, HeatMethod heat) {
        ArrayList<LowBowPca> low = lowbowM.getDocModels();
        LowBow curve = low.get(low.size() - 1);
        frameState.setText("Computing heat flow");
        curve.heatFlow(lambda, heat);
        frameState.setText("");
        if (simplexOnPolyState == 0) {
            buildPca(false);
        }
        if (simplexOnPolyState == 2) {
            simplexOnPolyState = 1;
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int mRotation = e.getWheelRotation();
        if (simplexOnPolyState > 0) {
            double percent = 0.1;
            double sizeX = engine.getXmax() - engine.getXmin();
            double sizeY = engine.getYmax() - engine.getYmin();
            engine.setCamera(engine.getXmin() - mRotation * percent * 0.5 * sizeX, engine.getXmax() + mRotation * percent * 0.5 * sizeX, engine.getYmin() - mRotation * percent * 0.5 * sizeY, engine.getYmax() + mRotation * percent * 0.5 * sizeY);
        }
    }

    public class myActionListener implements ActionListener {
        JFrame frame;

        public myActionListener(JFrame frame) {
            this.frame = frame;
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            boolean sigmaAuto = false;

            frameState.setText("Loading");
            if (frame.isVisible()) {
                frame.setVisible(false);
            }
            String inString = inOut.getText();
//            LowBowPca lowbow = new LowBowPca(inString, new MyTextSplitter());
            LowBowPca lowbow = new LowBowPca(inString, new SpaceSplitter());
            isReady = true;
            double sigma = 0.02;
            try {
                sigma = Double.parseDouble(sigmaText.getText());
            } catch (NumberFormatException e1) {
                if (sigmaText.getText().toLowerCase().equals("auto")) {
                    sigmaAuto = true;
                }
            }
            lowbow.setSamplesPerTextLength(samplesIn);
            if (sigmaAuto) {
                sigma = 1.0 / (2 * lowbow.getTextLength());
            }
            lowbow.setSigma(sigma);
            lowbow.setSmoothingCoeff(0.01);
            lowbowM.add(lowbow);
            frameState.setText("Processing");
            lowbowM.build();
            isReady = false;
            isProcess = true;
            ArrayList<LowBowPca> lLowBow = lowbowM.getDocModels();
            addCurveToEngine(lLowBow.get(lLowBow.size() - 1));
            frameState.setText("");
            frame.setVisible(true);
            if (simplexOnPolyState == 2) {
                simplexOnPolyState = 1;
            }
        }

    }

}
