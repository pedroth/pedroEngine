package graphicEngine;


public abstract class AbstractDrawAble implements DrawAble, Identifier, VolumeBounded {
    private static int nextId;
    private int id;

    public AbstractDrawAble() {
        this.id = nextId++;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public abstract void draw();
}
