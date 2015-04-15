package apps;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nlp.LowBow;
import nlp.LowBowManager;
import nlp.textSplitter.MyTextSplitter;
import nlp.textSplitter.SpaceSplitter;
import nlp.textSplitter.StopWordsSplitter;
import numeric.MyMath;
import tools.simple.Camera3D;
import tools.simple.TextFrame;
import twoDimEngine.FillShader;
import twoDimEngine.Line2D;
import twoDimEngine.PaintMethod2D;
import twoDimEngine.String2D;
import twoDimEngine.TwoDimEngine;
import algebra.Vec2;
import algebra.Vec3;

/**
 * 
 * @author pedro
 * 
 */
public class TextCurves extends MyFrame {
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
	/**
	 * locally weighted bag of wordsIndex
	 */
	private LowBowManager lowbowM;
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
	 * draw simplex on polygon
	 */
	private boolean simplexOnPoly;
	
	public TextCurves(String title, int width, int height) {
		super(title, width, height);
		engine = new TwoDimEngine(width, height);
		shader = new FillShader(engine);
		engine.setBackGroundColor(Color.white);
		engine.setCamera(-1, 1, -1, 1);
		/**
		 * GUI crap
		 */
		input = new JFrame("Input text File");
		input.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		input.setLayout(new GridLayout(1, 2));
		Panel p1 = new Panel(new GridLayout(6, 1));
		input.setResizable(false);
		input.setSize(500, 300);
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
		addButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});
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
		samplesSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider aux = (JSlider) e.getSource();
				samplesIn = samplesInMin + ((samplesInMax - samplesInMin) / (aux.getMaximum())) * (aux.getValue());
				samplesSliderTxt.setText("samples/txtLength:" + samplesIn);
			}
		});
		p2.add(samplesSlider);
		p1.add(p2);
		/**
		 * sigma text
		 */

		Panel p3 = new Panel(new GridLayout(1, 2));
		p3.add(new Label("Sigma[> 0]: "));
		sigmaText = new TextField();
		sigmaText.setText("" + 0.2);
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
		generateButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				generateText();
			}
		});
		Button heatFlowButton = new Button("Heat Flow");
		heatFlowButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				heatFlow();
			}
		});
		p5.add(heatFlowButton);
		p5.add(generateButton);
		p1.add(p5);
		
		/**
		 * Poly Simplex
		 */

		input.add(p1);

		input.setLocationRelativeTo(null);

		isReady = false;
		isProcess = false;
		input.setVisible(true);
		camera = new Camera3D();
		projCurves = new ArrayList<Vec2[]>();
		projLines = new ArrayList<Line2D[]>();
		strCurves = new ArrayList<String2D[]>();
		lowbowM = new LowBowManager();
		centerMass = new Vec3();
		stdev = 0;
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
		// TODO Auto-generated method stub

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
	private void updateCurve() {
		/**
		 * distance to plane
		 */
		double d = 1;
		ArrayList<LowBow> l = lowbowM.getLowbows();
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
				if (v.getZ() < d / 1000) {
					projLine[boundj].setVisible(false);
				} else {
					projLine[boundj].setVisible(true);
					projCurve[j].setX(v.getX() * (d / v.getZ()));
					projCurve[j].setY(v.getY() * (d / v.getZ()));
				}
			}
		}
	}

	private void updateStringCurve() {
		ArrayList<LowBow> l = lowbowM.getLowbows();
		int n = l.size();
		for (int i = 0; i < n; i++) {
			String2D[] strCurve = strCurves.get(i);
			LowBow lowbow = l.get(i);
			Vec2[] projCurve = projCurves.get(i);
			Line2D[] projLine = projLines.get(i);
			for (int j = 0; j < strCurve.length; j++) {
				int k = (int) Math.floor(MyMath.clamp(lowbow.getSamplesPerTextLength() * j, 0, lowbow.getSamples()));
				if (txtVisibleCheckBox.getState() && projLine[i].isVisible()) {
					strCurve[j].setVisible(true);
					strCurve[j].setVertex(projCurve[k], 0);
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
		ArrayList<LowBow> lowbows = lowbowM.getLowbows();
		LowBow lowbow = lowbows.get(lowbows.size() - 1);
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
	public void updateDraw(Graphics g) {
		engine.clearImageWithBackground();
		camera.update(dt);
		updateCurve();
		updateStringCurve();
		engine.drawElements();
		this.setTitle("Fps : " + this.getFps());
		engine.paintImage(g);
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
		Random r = new Random();
		Color color = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
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
		ArrayList<LowBow> lowList = lowbowM.getLowbows();

		if (lowList == null)
			return;

		LowBow low = lowList.get(lowList.size() - 1);
		int n = low.getTextLength();
		double s = low.getSamplesPerTextLength();
		int samples = low.getSamples();
		String acm = "";
		for (int i = 0; i < n; i++) {
			int k = (int) Math.floor(MyMath.clamp(s * i, 0, samples));
			acm += low.generateText(k) + "\n";
		}

		TextFrame frame = new TextFrame("Generated Text", acm);
	}

	public void pcaCoords() {
		ArrayList<LowBow> low = lowbowM.getLowbows();
		LowBow curve = low.get(low.size() - 1);
		Vec3[] pca = curve.getPcaCurve();
		String[] text = curve.getText();
		for (int i = 0; i < text.length; i++) {
			String string = text[i];
			int k = (int) Math.floor(MyMath.clamp(curve.getSamplesPerTextLength() * i, 0, curve.getSamples()));
			System.out.println(string + "\t" + pca[k].getX() + "\t" + pca[k].getY() + "\t" + pca[k].getZ());
		}
	}

	public void heatFlow() {
		ArrayList<LowBow> low = lowbowM.getLowbows();
		LowBow curve = low.get(low.size() - 1);
		frameState.setText("Computing heat flow");
		curve.heatFlow(0.1); // 0.5
		curve.curve2Heat();
		frameState.setText("build pca");
		lowbowM.buildPca();
		updateCurveStats();
		frameState.setText("");
	}

	public class myActionListener implements ActionListener {
		JFrame frame;

		public myActionListener(JFrame frame) {
			this.frame = frame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			frameState.setText("Loading");
			if (frame.isVisible()) {
				frame.setVisible(false);
			}
			String inString = inOut.getText();
			LowBow lowbow = new LowBow(inString, new StopWordsSplitter("src/nlp/wordsLists/stopWords.txt"));
			isReady = true;
			double sigma = 0.02;
			try {
				sigma = Double.parseDouble(sigmaText.getText());
			} catch (NumberFormatException e1) {
				/**
				 * blank on purpose
				 */
			}
			lowbow.setSamplesPerTextLength(samplesIn);
			lowbow.setSigma(sigma);
			lowbow.setSmoothingCoeff(0.01);
			lowbowM.add(lowbow);
			frameState.setText("Processing");
			lowbowM.init();
			frameState.setText("computing Pca");
			lowbowM.buildPca();
			isReady = false;
			isProcess = true;
			updateCurveStats();
			ArrayList<LowBow> lLowBow = lowbowM.getLowbows();
			addCurveToEngine(lLowBow.get(lLowBow.size() - 1));
			frameState.setText("");
			frame.setVisible(true);
		}

	}

	public static void main(String[] args) {
		new TextCurves("Teste", 800, 500);
	}

}
