package graphicEngine;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractGraphicEngine {
    private BufferedImage image;
    private Map<String, Object> workingMemory;

    public AbstractGraphicEngine(int width, int height) {
        setImageSize(width, height);
        workingMemory = new HashMap<>(1);
    }

    public void setImageSize(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public BufferedImage getImage() {
        return image;
    }

    public Graphics getGraphics() {
        return image.getGraphics();
    }

    public abstract void drawElements();

    public <E> E retrieve(String key) {
        return (E) workingMemory.get(key);
    }

    public void store(String key, Object element) {
        workingMemory.put(key, element);
    }

    public boolean containsKey(String key) {
        return workingMemory.containsKey(key);
    }
}
