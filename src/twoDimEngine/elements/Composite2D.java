package twoDimEngine.elements;

import java.util.ArrayList;
import java.util.List;

import twoDimEngine.AbstractDrawAble2D;
import twoDimEngine.BoundingBox;
import twoDimEngine.shaders.PaintMethod2D;

public class Composite2D extends AbstractDrawAble2D {
	private List<AbstractDrawAble2D> elementList;
	
	public Composite2D() {
		super();
		elementList = new ArrayList<AbstractDrawAble2D>();
	}
	
	@Override
	public void draw(PaintMethod2D painter) {
		for (AbstractDrawAble2D element : elementList) {
			element.draw(painter);
		}
	}

	@Override
	public BoundingBox getBoundingBox() {
		BoundingBox aux = new BoundingBox();
		if(elementList != null) {
			int size = elementList.size();
			aux = elementList.get(0).getBoundingBox();
			for(int i = 1; i < size; i++) {
				aux = BoundingBox.union(aux, elementList.get(i).getBoundingBox());
			}
		}
		return aux;
	}
}
