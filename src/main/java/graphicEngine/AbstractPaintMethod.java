package graphicEngine;

/**
 * The type Abstract paint method.
 */
public abstract class AbstractPaintMethod implements PaintMethod {
    private AbstractGraphicEngine engine;

    /**
     * Instantiates a new Abstract paint method.
     *
     * @param engine the engine
     */
    public AbstractPaintMethod(AbstractGraphicEngine engine) {
        this.engine = engine;
    }

    /**
     * Gets engine.
     *
     * @return the engine
     */
    public AbstractGraphicEngine getEngine() {
        return engine;
    }
}
