package graphicEngine;


public class BVEngine<D extends AbstractDrawAble> extends AbstractGraphicEngine {
    private BoundingVolume drawVolume;
    private BVManager<D> geometryManager;

    public BVEngine(int width, int height) {
        super(width, height);
        geometryManager = new BVManager();
    }

    public D getElement(int id) {
        return geometryManager.getElement(id);
    }

    public void addElement(D element) {
        geometryManager.addElement(element);
    }

    @Override
    public void drawElements() {
        drawElements(drawVolume);
    }

    public void drawElements(BoundingVolume drawVolume) {
        this.drawVolume = drawVolume;
        for (D drawAble : geometryManager.getElementsInVolume(drawVolume)) {
            drawAble.draw();
        }
    }
}
