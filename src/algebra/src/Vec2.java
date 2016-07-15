package algebra.src;

public class Vec2 extends Vector {

	public Vec2(double x, double y) {
		super(2);
		this.setX(1, x);
		this.setX(2, y);
	}

	/**
	 * 
	 * @param v
	 *            3 * 1 matrix
	 */
	public Vec2(Matrix v) {
		super(v);
	}

	public Vec2() {
		super(2);
		this.setX(1, 0.0);
		this.setX(2, 0.0);
	}

	public static Vec2 matrixProd(Matrix m, Vec2 v) {
		Vec2 ret;
		ret = new Vec2(Vector.matrixProd(m, v));
		return ret;
	}

	public static Vec2 add(Vec2 v1, Vec2 v2) {
		Vec2 ret = new Vec2(Vector.add(v1, v2));
		return ret;
	}

	public static Vec2 diff(Vec2 v1, Vec2 v2) {
		Vec2 ret = new Vec2(Vector.diff(v1, v2));
		return ret;
	}

	/**
	 * multiplication of real number with a vector, not the inner product
	 *
	 * @param x
	 *            real number
	 * @param v
	 *            vector
	 * @return
	 */
	public static Vec2 scalarProd(double x, Vec2 v) {
		Vec2 ret = new Vec2(Vector.scalarProd(x, v));
		return ret;
	}

	public static Vec2 normalize(Vec2 v) {
		Vec2 ret = new Vec2(Vector.normalize(v));
		return ret;
	}

	/**
	 *
	 * @param u
	 * @param v
	 * @return projection of u on v;
	 */
	public static Vec2 projection(Vec2 u, Vec2 v) {
		Vec2 ret = new Vec2(Vector.projection(u, v));
		return ret;
	}

	/*
	 * static
	 */

	/**
	 *
	 * @param u
	 * @param v
	 * @return return the orthogonal projection of u on v
	 */
	public static Vec2 orthoProjection(Vec2 u, Vec2 v) {
		Vec2 ret = new Vec2(Vector.orthoProjection(u, v));
		return ret;
	}

	public Vec2 copy() {
		Matrix m = super.copy();
		Vec2 r = new Vec2(m);
		return r;
	}

	public double getX() {
		return this.getX(1);
	}

	public void setX(double x) {
		this.setX(1, x);
	}

	public double getY() {
		return this.getX(2);
	}

	public void setY(double y) {
		this.setX(2, y);
	}

	public void setXY(double x, double y) {
		this.setX(1, x);
		this.setX(2, y);
	}

	public double getAngle() {
		return Math.atan2(this.getY(), this.getX());
	}

//	public static void main(String[] args) {
//		Vec2 v = new Vec2(1, 0);
//		v.setX(v.getX() + 1.0);
//	}
}
