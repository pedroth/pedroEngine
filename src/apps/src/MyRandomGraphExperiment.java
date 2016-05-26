package apps.src;

import algebra.src.Matrix;
import algebra.src.Vector;
import algebra.utils.MatrixPrinter;
import graph.Graph;
import inputOutput.MyText;
import numeric.src.MyMath;
import realFunction.src.ExpressionFunction;
import realFunction.src.MultiVarFunction;
import realFunction.src.UniVarFunction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MyRandomGraphExperiment {
    /*
     * GUI stuff
     */
    private JFrame frame = new JFrame("Grupo 1");

    private JPanel panel = new JPanel();

    private Choice functionType = new Choice();

    private TextField functionString = new TextField("sin(x) + cos(y)");

    private TextField xMinTxt = new TextField("0");

    private TextField xMaxTxt = new TextField("2 * pi");

    private TextField yMinTxt = new TextField("0");

    private TextField yMaxTxt = new TextField("2 * pi");

    private TextField numOfVertexTxt = new TextField("100");

    private TextField numberOfSamplesTxt = new TextField("1000");

    private Button runButton = new Button("Run");

    private Choice positiveFunctionCombo = new Choice();

    private TextField scaleTxt = new TextField("1.0");

    private Label log = new Label();

    /*
     * Stuff from GUI
     */
    private String expr = "0.5";

    private int numVertex = 10;

    private int samples = 1;

    private double xmin = 0;

    private double xmax = 2 * Math.PI;

    private double ymin = 0;

    private double ymax = 2 * Math.PI;

    private UniVarFunction positiveFunction;

    /**
     * Here for printing purposes
     */
    private String originalRandomMatrix;

    /**
     * between 0 and 1
     */
    private double scale = 1.0;

    public MyRandomGraphExperiment() {
        /*
         * build GUI
		 */
        this.BuildGUI();
        functionType.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (functionType.getSelectedItem().equals("Random")) {
                    functionString.setVisible(false);
                    xMinTxt.setVisible(false);
                    xMaxTxt.setVisible(false);
                    yMinTxt.setVisible(false);
                    yMaxTxt.setVisible(false);
                } else if (functionType.getSelectedItem().equals("Constant")) {
                    functionString.setVisible(false);
                    functionString.setText("1.0");
                    xMinTxt.setVisible(false);
                    xMaxTxt.setVisible(false);
                    yMinTxt.setVisible(false);
                    yMaxTxt.setVisible(false);
                } else {
                    functionString.setVisible(true);
                    xMinTxt.setVisible(true);
                    xMaxTxt.setVisible(true);
                    yMinTxt.setVisible(true);
                    yMaxTxt.setVisible(true);
                }
            }
        });

        runButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String xminStr = xMinTxt.getText();
                expr = functionString.getText();
                xmin = MyMath.numericRead(xMinTxt.getText());
                xmax = MyMath.numericRead(xMaxTxt.getText());
                ymin = MyMath.numericRead(yMinTxt.getText());
                ymax = MyMath.numericRead(yMaxTxt.getText());
                numVertex = (int) Math.max(1, Math.floor(MyMath.numericRead(numOfVertexTxt.getText())));
                samples = (int) Math.max(1, Math.floor(MyMath.numericRead(numberOfSamplesTxt.getText())));

                scale = MyMath.clamp(MyMath.numericRead(scaleTxt.getText()), 0, 1);

                switch (positiveFunctionCombo.getSelectedItem()) {
                    case "abs":
                        positiveFunction = x -> Math.abs(x);
                        break;

                    case "max":
                        positiveFunction = x -> Math.max(x, 0);
                        break;

                    case "square":
                        positiveFunction = x -> x * x;
                        break;
                }

                runExperiments();
            }

        });
    }

    public static void main(String[] args) {
        new MyRandomGraphExperiment();
    }

    /*
     * build GUI code UGLY!
     */
    private void BuildGUI() {
        frame.setSize(500, 500);
        panel.setLayout(new GridLayout(10, 1));
        JPanel functionPanel = new JPanel(new GridLayout(1, 2));
        functionPanel.add(functionType);
        functionType.add("F(x,y)");
        functionType.add("Random");
        functionType.add("Constant");
        functionPanel.add(functionString);
        panel.add(functionPanel);

        JPanel normalizationPanel = new JPanel(new GridLayout(1, 2));
        normalizationPanel.add(new Label("Scale [0, 1]"));
        normalizationPanel.add(scaleTxt);
        panel.add(normalizationPanel);

        JPanel comboxPanel = new JPanel(new GridLayout(1, 2));
        comboxPanel.add(new Label("Choose positive function"));
        comboxPanel.add(positiveFunctionCombo);
        positiveFunctionCombo.add("abs");
        positiveFunctionCombo.add("max");
        positiveFunctionCombo.add("square");
        positiveFunctionCombo.select(1);
        panel.add(comboxPanel);
        JPanel intervalPanelX = new JPanel(new GridLayout(1, 2));
        intervalPanelX.add(new Label("xmin"));
        intervalPanelX.add(new Label("xmax"));
        panel.add(intervalPanelX);
        JPanel intervalPanelXValue = new JPanel(new GridLayout(1, 2));
        intervalPanelXValue.add(xMinTxt);
        intervalPanelXValue.add(xMaxTxt);
        panel.add(intervalPanelXValue);
        JPanel intervalPanelY = new JPanel(new GridLayout(1, 2));
        intervalPanelY.add(new Label("ymin"));
        intervalPanelY.add(new Label("ymax"));
        panel.add(intervalPanelY);
        JPanel intervalPanelYValue = new JPanel(new GridLayout(1, 2));
        intervalPanelYValue.add(yMinTxt);
        intervalPanelYValue.add(yMaxTxt);
        panel.add(intervalPanelYValue);
        JPanel numOfVertexPanel = new JPanel(new GridLayout(1, 2));
        numOfVertexPanel.add(new Label("Number of vertex [ > 0 ]"));
        numOfVertexPanel.add(numOfVertexTxt);
        panel.add(numOfVertexPanel);
        JPanel numOfSamplesPanel = new JPanel(new GridLayout(1, 2));
        numOfSamplesPanel.add(new Label("Number of samples [ > 0 ]"));
        numOfSamplesPanel.add(numberOfSamplesTxt);
        panel.add(numOfSamplesPanel);
        JPanel runPanel = new JPanel(new GridLayout(1, 2));
        runPanel.add(log);
        runPanel.add(runButton);
        panel.add(runPanel);
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private Matrix generateRandomMatrix() {
        String[] vars = {"x", "y"};
        MultiVarFunction function = null;
        if (functionType.getSelectedItem().equals("Random")) {
            function = new MultiVarFunction(0) {
                private Random random = new Random();

                @Override
                public double compute(Vector x) {
                    return random.nextDouble();
                }
            };
        } else {
            ExpressionFunction fAux = new ExpressionFunction(expr, vars);
            fAux.init();
            function = fAux;
        }

        Matrix randomMatrix = new Matrix(numVertex, numVertex);
        double[] x = new double[2];
        double maxValue = Double.MIN_VALUE;
        for (int i = 1; i <= numVertex; i++) {
            for (int j = i + 1; j <= numVertex; j++) {
                x[0] = xmin + ((j - 1.0) / (numVertex - 1)) * (xmax - xmin);
                x[1] = ymax + ((i - 1.0) / (numVertex - 1)) * (ymin - ymax);
                double f = function.compute(new Vector(x));
                f = positiveFunction.compute(f);
                maxValue = Math.max(f, maxValue);
                randomMatrix.setXY(i, j, f);
                randomMatrix.setXY(j, i, f);
            }
        }

        for (int i = 1; i <= numVertex; i++) {
            for (int j = i + 1; j <= numVertex; j++) {
                double f = (randomMatrix.getXY(i, j) / maxValue) * scale;
                randomMatrix.setXY(i, j, f);
                randomMatrix.setXY(j, i, f);
            }
        }
        originalRandomMatrix = randomMatrix.toString(new MatlabMatrixPrinter());
        return randomMatrix;
    }

    private Graph generateRandomGraph(Matrix randomMatrix) {
        Random r = new Random();
        int numVertex = randomMatrix.getRows();
        for (int i = 1; i <= numVertex; i++) {
            for (int j = i + 1; j <= numVertex; j++) {
                double p = r.nextDouble();
                double edge = p <= randomMatrix.getXY(i, j) ? 1.0 : 0.0;
                randomMatrix.setXY(i, j, edge);
                randomMatrix.setXY(j, i, edge);
            }
        }
        return new Graph(randomMatrix);
    }

    private void degreeDistributionStatistics(Graph graph, List<Double> data) {
        List<Integer> degreeValues = graph.getDegreeValues();
        for (Integer i : degreeValues) {
            data.add((double) i);
        }
    }

    private void clusterCoefficientStatistics(Graph graph, List<Double> data) {
        List<Double> clusterCoeffsValues = graph.getClusterCoefficients();
        for (Double x : clusterCoeffsValues) {
            data.add((double) x);
        }
    }

    private void distanceDistributionStatistics(Graph graph, List<Double> data) {
        for (Integer v : graph.getVertexSet()) {
            Map<Integer, Double> distancesFromV = graph.getDistancesFrom(v);
            for (Double x : distancesFromV.values()) {
                if (x < Double.MAX_VALUE) {
                    data.add(x);
                }
            }
        }
    }

    private void runExperiments() {
        List<Double> degreeData = new ArrayList<Double>((int) (numVertex * samples));
        List<Double> clusteringCoeffData = new ArrayList<Double>((int) (numVertex * samples));
        List<Double> distancesData = new ArrayList<Double>((int) (numVertex * numVertex * samples));

        Matrix randomMatrix = this.generateRandomMatrix();
        Graph graph = null;
        System.out.println("COMPUTATION");
        for (int i = 0; i < this.samples; i++) {
            graph = generateRandomGraph(randomMatrix);
            degreeDistributionStatistics(graph, degreeData);
            clusterCoefficientStatistics(graph, clusteringCoeffData);
            distanceDistributionStatistics(graph, distancesData);
            System.out.println(String.format("%.1f", Math.floor(((1.0 * i) / this.samples) * 100)));
            // log.repaint();
        }
        StringBuilder degrees = new StringBuilder(numVertex * samples);
        StringBuilder clusterCoeffs = new StringBuilder(numVertex * samples);
        StringBuilder distances = new StringBuilder(numVertex * numVertex * samples);
        degrees.append("degrees = [ ");
        clusterCoeffs.append("clusterCoefs = [ ");
        distances.append("distances = [ ");
        System.out.println("STRING CONSTRUCTION");
        for (int i = 0; i < degreeData.size(); i++) {
            degrees.append(degreeData.get(i) + (i <= (degreeData.size() - 2) ? " ; " : " ];"));
            clusterCoeffs.append(clusteringCoeffData.get(i) + (i <= (degreeData.size() - 2) ? " ; " : " ];"));
            System.out.println(String.format("%.1f", Math.floor(((1.0 * i) / degreeData.size()) * 100)));
        }
        for (int i = 0; i < distancesData.size(); i++) {
            distances.append(distancesData.get(i) + (i <= (distancesData.size() - 2) ? " ; " : " ];"));
            System.out.println(String.format("%.1f", Math.floor(((1.0 * i) / distancesData.size()) * 100)));
        }
        File desktop = new File(System.getProperty("user.home"), "Desktop");
        String desktopAddress = desktop.getAbsolutePath();
        MyText text = new MyText();
        degrees.append(String.format("%n"));
        degrees.append(clusterCoeffs);
        degrees.append(String.format("%n"));
        degrees.append("randomMat = " + originalRandomMatrix + " ;");
        degrees.append(String.format("%n"));
        degrees.append(distances);
        degrees.append(String.format("%n"));
        degrees.append("graphMatrix = " + graph.getAdjacencyMatrix().toString(new MatlabMatrixPrinter()) + " ;");

        text.write(desktopAddress + File.separator + "graphStats.m", degrees.toString());
//		text.write(desktopAddress + File.separator + "graphStats.csv", graph.toStringGephi());
    }

    class MatlabMatrixPrinter implements MatrixPrinter {

        @Override
        public String before() {
            return "[";
        }

        @Override
        public String after() {
            return "]";
        }

        @Override
        public String line() {
            return ";";
        }

        @Override
        public String separator() {
            return ",";
        }
    }

}
