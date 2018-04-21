package algebra;

public class Vec3 extends Vector {

	public Vec3(double x, double y, double z) {
		super(3);
		this.setX(1, x);
		this.setX(2, y);
		this.setX(3, z);
	}

	/**
	 * 
	 * @param v
	 *            3 * 1 matrix
	 */
	public Vec3(Matrix v) {
		super(v);
	}

	public Vec3() {
		super(3);
		this.setX(1, 0.0);
		this.setX(2, 0.0);
		this.setX(3, 0.0);
	}

	public static Vec3 matrixProd(Matrix m, Vec3 v) {
		Vec3 ret;
		ret = new Vec3(Vector.matrixProd(m, v));
		return ret;
	}

	public static Vec3 add(Vec3 v1, Vec3 v2) {
		Vec3 ret = new Vec3(Vector.add(v1, v2));
		return ret;
	}

	public static Vec3 diff(Vec3 v1, Vec3 v2) {
		Vec3 ret = new Vec3(Vector.diff(v1, v2));
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
	public static Vec3 scalarProd(double x, Vec3 v) {
		Vec3 ret = new Vec3(Vector.scalarProd(x, v));
		return ret;
	}

	public static Vec3 normalize(Vec3 v) {
		Vec3 ret = new Vec3(Vector.normalize(v));
		return ret;
	}

	public static Vec3 vectorProduct(Vec3 v1, Vec3 v2) {
		Vec3 aux;
		aux = new Vec3(v1.getY() * v2.getZ() - v1.getZ() * v2.getY(), v1.getZ()
				* v2.getX() - v1.getX() * v2.getZ(), v1.getX() * v2.getY()
				- v1.getY() * v2.getX());
		return aux;
	}

	/**
	 *
	 * @param u
	 * @param v
	 * @return projection of u on v;
	 */
	public static Vec3 projection(Vec3 u, Vec3 v) {
		Vec3 ret = new Vec3(Vector.projection(u, v));
		return ret;
	}

	/**
	 *
	 * @param u
	 * @param v
	 * @return return the orthogonal projection of u on v
	 */
	public static Vec3 orthoProjection(Vec3 u, Vec3 v) {
		Vec3 ret = new Vec3(Vector.orthoProjection(u, v));
		return ret;
	}

	/*
	 * static
	 */

	public Vec3 copy() {
		Matrix m = super.copy();
		Vec3 r = new Vec3(m);
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

	public double getZ() {
		return this.getX(3);
	}

	public void setZ(double z) {
		this.setX(3, z);
	}

	public void setXYZ(double x, double y, double z) {
		this.setX(1, x);
		this.setX(2, y);
		this.setX(3, z);
	}

}
