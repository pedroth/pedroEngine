package twoDimEngine;

import algebra.Matrix;
import algebra.Vec2;
import algebra.Vector;
import numeric.MyMath;

public class BoundingBox {
	private double xmin, ymax, xmax, ymin;
	private double xcenter, ycenter;
	private boolean empty;

	public BoundingBox(double xmin, double ymin, double xmax, double ymax) {
		super();
		this.xmin = xmin;
		this.ymax = ymax;
		this.xmax = xmax;
		this.ymin = ymin;
		xcenter = (xmin + xmax) / 2.0;
		ycenter = (ymin + ymax) / 2.0;
		empty = checkIfEmpty();
	}

	public BoundingBox() {
		empty = true;
	}

	public static BoundingBox union(BoundingBox r1, BoundingBox r2) {
		if (r1.isEmpty()) {
			return r2;
		} else if (r2.isEmpty()) {
			return r1;
		} else {
			double xmin = (Math.min(r1.getXmin(), r2.getXmin()));
			double xmax = (Math.max(r1.getXmax(), r2.getXmax()));
			double ymin = (Math.min(r1.getYmin(), r2.getYmin()));
			double ymax = (Math.max(r1.getYmax(), r2.getYmax()));
			return new BoundingBox(xmin, ymin, xmax, ymax);
		}
	}

	/**
	 * @param r1
	 * @param r2
	 * @return null if rectangles do not intersect
	 */
	public static BoundingBox intersection(BoundingBox r1, BoundingBox r2) {
		if (r1.isEmpty() || r2.isEmpty()) {
			return new BoundingBox();
		} else {
			double xmin = (Math.max(r1.getXmin(), r2.getXmin()));
			double xmax = (Math.min(r1.getXmax(), r2.getXmax()));
			double ymin = (Math.max(r1.getYmin(), r2.getYmin()));
			double ymax = (Math.min(r1.getYmax(), r2.getYmax()));
			return new BoundingBox(xmin, ymin, xmax, ymax);
		}
	}

	public static BoundingBox scale(BoundingBox r, double s) {
		Vec2 center = r.getCenter();
		double xcenter = center.getX();
		double ycenter = center.getY();
		BoundingBox box = new BoundingBox(
				xcenter + (r.getXmin() - xcenter) * s, ycenter
				+ (r.getYmin() - ycenter) * s, xcenter
				+ (r.getXmax() - xcenter) * s, ycenter
				+ (r.getYmax() - ycenter) * s);
		return box;
	}

	public static BoundingBox translate(BoundingBox r, Vec2 v) {
		double xcenter = v.getX();
		double ycenter = v.getY();

		BoundingBox box = new BoundingBox(xcenter + r.getXmin(), ycenter
				+ r.getYmin(), xcenter + r.getXmax(), ycenter + r.getYmax());

		return box;
	}

	private boolean checkIfEmpty() {
		return !(xmax >= xmin && ymax >= ymin);
	}

	public double getXmin() {
		return xmin;
	}

	public void setXmin(double xmin) {
		this.xmin = xmin;
		xcenter = (xmin + xmax) / 2.0;
		empty = checkIfEmpty();
	}

	public double getYmax() {
		return ymax;
	}

	public void setYmax(double ymax) {
		this.ymax = ymax;
		ycenter = (ymin + ymax) / 2.0;
		empty = checkIfEmpty();
	}

	public double getXmax() {
		return xmax;
	}

	public void setXmax(double xmax) {
		this.xmax = xmax;
		xcenter = (xmin + xmax) / 2.0;
		empty = checkIfEmpty();
	}

	public double getYmin() {
		return ymin;
	}

	public void setYmin(double ymin) {
		this.ymin = ymin;
		ycenter = (ymin + ymax) / 2.0;
	}

	public double getXcenter() {
		return xcenter;
	}

	public void setXcenter(double xcenter) {
		this.xcenter = xcenter;
	}

	public double getYcenter() {
		return ycenter;
	}

	public void setYcenter(double ycenter) {
		this.ycenter = ycenter;
	}

	public Vec2 getCenter() {
		return new Vec2(xcenter, ycenter);
	}

	public Vec2 getXYmin() {
		return new Vec2(xmin, ymin);
	}

	public Vec2 getXYmax() {
		return new Vec2(xmax, ymax);
	}

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

	public Vec2[] getVertex() {
		Vec2[] vec = new Vec2[4];
		vec[0] = new Vec2(xmin, ymin);
		vec[1] = new Vec2(xmax, ymin);
		vec[2] = new Vec2(xmax, ymax);
		vec[3] = new Vec2(xmin, ymax);
		return vec;
	}

	/**
	 *
	 * @param x
	 * @return return four dimensional vector where each dimension correspond to
	 *         a barycentric coordinate. first dimension - left down corner
	 *         second dimension - right down corner third dimension - right up
	 *         corner fourth dimension - left up corner
	 */
	public Vector getBarycentricCoord(Vec2 x) {
		Matrix m;
		Vec2[] u = new Vec2[3];
		Vec2[] v = getVertex();
		for (int i = 0; i < u.length; i++) {
			u[i] = Vec2.diff(v[i + 1], v[0]);
		}
		m = u[0];
		for (int i = 1; i < u.length; i++) {
			m = m.concat(u[i]);
		}
		x = Vec2.diff(x, v[0]);
		Vector ans = Matrix.leastSquareLinearSystem(m, x, 1E-10);
		Vector res = new Vector(4);
		for (int i = 1; i <= 4; i++) {
			if (i == 1) {
				res.setX(1, 1 - ans.getX(1) - ans.getX(2) - ans.getX(3));
			} else {
				res.setX(i, ans.getX(i - 1));
			}
		}
		return res;
	}

	/**
	 *
	 * @return normal vector pointing out to each vertex of the bounding box
	 */
	public Vec2[] getNormals() {
		Vec2[] v = getVertex();
		Vec2[] ans = new Vec2[v.length];
		for (int i = 0; i < v.length; i++) {
			ans[i] = Vec2.normalize(Vec2.add(
					Vec2.normalize(Vec2.diff(v[i], v[MyMath.positiveMod(i - 1, v.length)])),
					Vec2.normalize(Vec2.diff(v[i], v[(i + 1) % v.length]))));
		}
		return ans;
	}

	/**
	 *
	 * @param x
	 * @return distance function from the box to x, in the interior of the box
	 *         the function is negative
	 */
	public double getDistance(Vec2 x) {
		Vec2[] vertex = getVertex();
		double minDistance = Double.MAX_VALUE;
		int index = 0;
		for (int i = 0; i < vertex.length; i++) {
			double distance = Vec2.diff(x, vertex[i]).norm();
			if(minDistance > distance){
				minDistance = distance;
				index = i;
			}
		}
		minDistance = Math.min(minDistance, lineDistance(vertex[MyMath.positiveMod(index - 1, vertex.length)], vertex[index], x));
		minDistance = Math.min(minDistance, lineDistance(vertex[index], vertex[(index + 1) % vertex.length], x));
		return minDistance;
	}

	private double lineDistance(Vec2 p1, Vec2 p2, Vec2 x) {
		Vec2 u = Vec2.diff(x, p1);
		Vec2 v = Vec2.diff(p2,p1);
		double norm = v.squareNorm();
		double dot = Vec2.innerProd(u, v) / norm;
		if(dot <= 0.0) {
			return u.norm();
		}else if(dot >= 1.0) {
			return Vec2.diff(x,p2).norm();
		}else {
			return Vec2.innerProd(Vec2.normalize(new Vec2(v.getY(),-v.getX())), u);
		}
	}

	/**
	 *
	 * @param x
	 * @return return gradient of distance function which is just the unit
	 *         normal vector to the box
	 */
	public Vec2 getNormal(Vec2 x) {
		Vec2[] normals = getNormals();
		Vector bary = getBarycentricCoord(x);
		Vec2 normal = new Vec2();
		for (int i = 0; i < normals.length; i++) {
			normal = Vec2.add(normal,
					Vec2.scalarProd(bary.getX(i + 1), normals[i]));
		}
		return Vec2.normalize(normal);
	}

}
