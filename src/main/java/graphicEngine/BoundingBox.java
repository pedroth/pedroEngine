package graphicEngine;

import algebra.Vector;
import algebra.utils.AlgebraException;

public class BoundingBox extends BoundingVolume<BoundingBox> {
    private Vector posCenter;
    private Vector posMin, posMax;


    public BoundingBox(Vector posMin, Vector posMax) {
        initialize(posMin, posMax);
    }

    public BoundingBox(BoundingBox boundingBox) {
        initialize(boundingBox.getPosMin().copy(), boundingBox.getPosMax().copy());
    }

    public BoundingBox() {
        this.isEmpty = true;
    }

    private void initialize(Vector posMin, Vector posMax) {
        this.posMin = posMin;
        this.posMax = posMax;
        this.posCenter = Vector.scalarProd(0.5, Vector.add(posMin, posMax));
        this.isEmpty = Vector.squareNorm(Vector.diff(posMin, posMax)) == 0;
    }

    public Vector getPosCenter() {
        return posCenter;
    }

    public Vector getPosMin() {
        return posMin;
    }

    public Vector getPosMax() {
        return posMax;
    }

    @Override
    public BoundingBox union(BoundingBox boundingVolume) {
        if (this.isEmpty()) {
            return new BoundingBox(boundingVolume);
        } else if (boundingVolume.isEmpty()) {
            return new BoundingBox(this);
        } else {
            Vector posMin = pointwiseMin(this.getPosMin(), boundingVolume.getPosMin());
            Vector posMax = pointwiseMax(this.getPosMax(), boundingVolume.getPosMax());
            return new BoundingBox(posMin, posMax);
        }
    }

    @Override
    public BoundingBox intersection(BoundingBox boundingVolume) {
        if (this.isEmpty() || boundingVolume.isEmpty()) {
            return new BoundingBox();
        } else {
            Vector posMin = pointwiseMax(this.getPosMin(), boundingVolume.getPosMin());
            Vector posMax = pointwiseMin(this.getPosMax(), boundingVolume.getPosMax());
            return new BoundingBox(posMin, posMax);
        }
    }

    @Override
    public double dist(BoundingBox boundingBox) {
        return Vector.diff(this.getPosCenter(), boundingBox.getPosCenter()).squareNorm();
    }

    private Vector pointwiseMin(Vector x, Vector y) {
        int dim = x.getDim();
        if (dim != y.getDim()) {
            throw new AlgebraException("Vector must be of same dimension");
        }
        Vector ans = new Vector(dim);
        for (int i = 1; i <= dim; i++) {
            ans.setX(i, Math.min(x.getX(i), y.getX(i)));
        }
        return ans;
    }


    private Vector pointwiseMax(Vector x, Vector y) {
        int dim = x.getDim();
        if (dim != y.getDim()) {
            throw new AlgebraException("Vector must be of same dimension");
        }
        Vector ans = new Vector(dim);
        for (int i = 1; i <= dim; i++) {
            ans.setX(i, Math.max(x.getX(i), y.getX(i)));
        }
        return ans;
    }
}
