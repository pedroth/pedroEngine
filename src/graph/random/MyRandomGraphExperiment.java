package graph.random;

import graph.Graph;
import inputOutput.MyText;

import java.awt.Button;
import java.awt.Choice;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import numeric.MyMath;
import realFunction.ExpressionFunction;
import realFunction.UniVarFunction;
import algebra.Matrix;
import algebra.Vector;

public class MyRandomGraphExperiment {
	/*
	 * GUI stuff
	 */
	private JFrame frame = new JFrame("Grupo 1");
	private JPanel panel = new JPanel();
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
	 * between 0 and 1
	 */
	private double scale = 1.0;

	public MyRandomGraphExperiment() {
		/*
		 * build GUI
		 */
		this.BuildGUI();
		runButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String xminStr = xMinTxt.getText();
				expr = functionString.getText();
				xmin = MyMath.numericRead(xMinTxt.getText());
				xmax = MyMath.numericRead(xMaxTxt.getText());
				ymin = MyMath.numericRead(yMinTxt.getText());
				ymax = MyMath.numericRead(yMinTxt.getText());
				numVertex = (int) Math.max(1 , Math.floor(MyMath.numericRead(numOfVertexTxt.getText())));
				samples = (int) Math.max(1 , Math.floor(MyMath.numericRead(numberOfSamplesTxt.getText())));
				
				scale = MyMath.clamp(MyMath.numericRead(scaleTxt.getText()), 0, 1);
				
				switch (positiveFunctionCombo.getSelectedItem()) {
				case "abs":
					positiveFunction = new UniVarFunction() {

						@Override
						public double compute(double x) {
							return Math.abs(x);
						}
					};
					break;

				case "max":
					positiveFunction = new UniVarFunction() {

						@Override
						public double compute(double x) {
							return Math.max(x, 0);
						}
					};
					break;

				case "square":
					positiveFunction = new UniVarFunction() {

						@Override
						public double compute(double x) {
							return x * x;
						}
					};
					break;
				}
				
				runExperiments();
			}

		});
	}

	/*
	 * build GUI code UGLY!
	 */
	private void BuildGUI() {
		frame.setSize(500, 500);
		panel.setLayout(new GridLayout(10, 1));
		JPanel functionPanel = new JPanel(new GridLayout(1, 2));
		functionPanel.add(new Label("F(x,y)"));
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
		numOfVertexPanel.add(new Label("Number of vertex"));
		numOfVertexPanel.add(numOfVertexTxt);
		panel.add(numOfVertexPanel);
		JPanel numOfSamplesPanel = new JPanel(new GridLayout(1, 2));
		numOfSamplesPanel.add(new Label("Number of samples"));
		numOfSamplesPanel.add(numberOfSamplesTxt);
		panel.add(numOfSamplesPanel);
		JPanel runPanel = new JPanel(new GridLayout(1, 2));
		runPanel.add(new Label(""));
		runPanel.add(runButton);
		panel.add(runPanel);
		frame.add(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private Matrix generateRandomMatrix() {
		String[] vars = { "x", "y" };
		ExpressionFunction function = new ExpressionFunction(expr, vars);
		function.init();
		Matrix randomMatrix = new Matrix(numVertex, numVertex);
		double[] x = new double[2];
		double maxValue = Double.MIN_VALUE;
		for (int i = 1; i <= numVertex; i++) {
			for (int j = i + 1; j <= numVertex; j++) {
				x[0] = xmin + ((i - 1.0) / (numVertex - 1)) * (xmax - xmin);
				x[1] = ymin + ((j - 1.0) / (numVertex - 1)) * (ymax - ymin);
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

	private void degreeDistributionStatistics(Graph graph, ArrayList<Double> data) {
		List<Integer> degreeValues = graph.getDegreeValues();
		for (Integer i : degreeValues)
			data.add((double) i);
	}

	private void clusterCoefficientStatistics(Graph graph, ArrayList<Double> data) {
		List<Double> clusterCoeffsValues = graph.getClusterCoefficients();
		for (Double x : clusterCoeffsValues)
			data.add((double) x);
	}

	private void runExperiments() {
		ArrayList<Double> degreeData = new ArrayList<Double>((int) (numVertex * samples));
		ArrayList<Double> clusteringCoeffData = new ArrayList<Double>((int) (numVertex * samples));
		Matrix randomMatrix = this.generateRandomMatrix();
		Graph graph = null;
		for (int i = 0; i < this.samples; i++) {
			graph = generateRandomGraph(randomMatrix);
			degreeDistributionStatistics(graph, degreeData);
			clusterCoefficientStatistics(graph, clusteringCoeffData);
		}

		String degrees = "degrees = [ ";
		String clusterCoeffs = "clusterCoefs = [ ";
		for (int i = 0; i < degreeData.size(); i++) {
			degrees += degreeData.get(i) + (i <= (degreeData.size() - 2) ? " ; " : " ];");
			clusterCoeffs += clusteringCoeffData.get(i) + (i <=(degreeData.size() - 2) ? " ; " : " ];");
		}
		File desktop = new File(System.getProperty("user.home"), "Desktop");
		String desktopAddress = desktop.getAbsolutePath();
		MyText text = new MyText();
		degrees += String.format("%n");
		degrees += clusterCoeffs;
		text.write(desktopAddress + File.separator + "graphStats.txt", degrees);
		text.write(desktopAddress + File.separator + "graphStats.csv", graph.toStringGephi());
	}

	public static void main(String[] args) {
		new MyRandomGraphExperiment();
	}

}
