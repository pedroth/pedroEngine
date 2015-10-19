package twoDimEngine;

import java.util.ArrayList;

import twoDimEngine.shaders.PaintMethod2D;

public class TwoDimEngine extends AbstractEngine2D{
	private ArrayList<AbstractDrawAble2D> elements;

	public TwoDimEngine(int width, int height) {
		super(width,height);
		elements = new ArrayList<AbstractDrawAble2D>();
	}

	public void addtoList(AbstractDrawAble2D e) {
		elements.add(e);
	}
	
	public void addtoList(AbstractDrawAble2D e, PaintMethod2D painter) {
		e.setMyPainter(painter);
		elements.add(e);
	}

	public void removeAllElements() {
		elements.removeAll(elements);
	}
	
	public void removeElement(AbstractDrawAble2D obj) {
		elements.remove(obj);
	}

	/**
	 * initializations before each draw
	 */
	private void initDraw() {
		// blank on purpose
	}

	public void drawElements() {
		int index = 0;
		initDraw();
		for (AbstractDrawAble2D e : elements) {
			if(e.isVisible() && !e.isDestroyed()) {
				draw(e);
			}else if(e.isDestroyed()){
				elements.remove(index);
			}
			index++;
		}
	}

	private void draw(AbstractDrawAble2D e) {
		PaintMethod2D painter = e.getMyPainter();
		if (e.getMyPainter() != null) {
			e.draw(painter);
		} else {
			e.draw(defaultPainter);
		}
	}

//	public void drawPxl(int x, int y, Color c) {
//		int[] pixels = ((java.awt.image.DataBufferInt) image.getRaster()
//				.getDataBuffer()).getData();
//		int w = image.getWidth();
//		if(x >= 0 && x < w && y >= 0 && y < image.getHeight() ) {
//			pixels[x * w + y] = c.getRGB();
//		}
//	}
}
