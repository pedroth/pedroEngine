package numeric.src;

import algebra.src.Matrix;
import algebra.src.Vec3;

/**
 * The type Camera 3 d.
 */
public class Camera3D {
	/**
	 * camera dynamics
	 */
	/**
	 * raw is 3-dim vector where 1st coordinate is distance to focalPoint 2nd
	 * coordinate is theta 3rd coordinate is phi
	 */
	private Vec3 raw;
	/**
	 * velRaw is the velocity of raw
	 */
	private Vec3 velRaw;
	/**
	 * where the camera is looking
	 */
	private Vec3 focalPoint;
	/**
	 * thrust on (raw,theta,phi);
	 */
	private Vec3 thrust;
	
	/**
	 * camera coordinate system
	 */
	private Vec3 eye;
	private Matrix camBasis;
	private Matrix inverseCamBasis;

	/**
	 * Instantiates a new Camera 3 d.
	 */
	public Camera3D() {
		raw = new Vec3(1, 0, 0);
		velRaw = new Vec3();
		focalPoint = new Vec3();
		thrust = new Vec3();
		eye = new Vec3();
		camBasis = new Matrix(3,3);
	}
	
	private void orbit() {
		double t = raw.getY();
		double p = raw.getZ();

		double cosP = Math.cos(p);
		double cosT = Math.cos(t);
		double sinP = Math.sin(p);
		double sinT = Math.sin(t);

		// z - axis
		camBasis.setXY(1, 3, -cosP * cosT);
		camBasis.setXY(2, 3, -cosP * sinT);
		camBasis.setXY(3, 3, -sinP);
		// y - axis
		camBasis.setXY(1, 2, -sinP * cosT);
		camBasis.setXY(2, 2, -sinP * sinT);
		camBasis.setXY(3, 2, cosP);
		// x -axis
		camBasis.setXY(1, 1, -sinT);
		camBasis.setXY(2, 1, cosT);
		camBasis.setXY(3, 1, 0);

		double r = raw.getX();
		eye = new Vec3(r * cosP * cosT + focalPoint.getX(), r
				* cosP * sinT + focalPoint.getY(), r * sinP + focalPoint.getZ());
		inverseCamBasis = Matrix.transpose(camBasis);
	}

	/**
	 * Update camera position.
	 *
	 * @param dt the dt
	 */
	public void update(double dt) {
		double accX = (thrust.getX() - velRaw.getX());
		double accY = (thrust.getY() - velRaw.getY());
		double accZ = (thrust.getZ() - velRaw.getZ());
		velRaw.setX(velRaw.getX() + accX * dt);
		velRaw.setY(velRaw.getY() + accY * dt);
		velRaw.setZ(velRaw.getZ() + accZ * dt);
		raw.setX(raw.getX() + velRaw.getX() * dt + 0.5 * accX * dt * dt);
		raw.setY(raw.getY() + velRaw.getY() * dt + 0.5 * accY * dt * dt);
		raw.setZ(raw.getZ() + velRaw.getZ() * dt + 0.5 * accZ * dt * dt);
		orbit();
	}

	/**
	 * Gets raw.
	 *
	 * @return the raw
	 */
	public Vec3 getRaw() {
		return raw;
	}

	/**
	 * Sets raw.
	 *
	 * @param raw the raw
	 */
	public void setRaw(Vec3 raw) {
		this.raw = raw;
	}

	/**
	 * Gets vel raw.
	 *
	 * @return the vel raw
	 */
	public Vec3 getVelRaw() {
		return velRaw;
	}

	/**
	 * Sets vel raw.
	 *
	 * @param velRaw the vel raw
	 */
	public void setVelRaw(Vec3 velRaw) {
		this.velRaw = velRaw;
	}

	/**
	 * Gets focal point.
	 *
	 * @return the focal point
	 */
	public Vec3 getFocalPoint() {
		return focalPoint;
	}

	/**
	 * Sets focal point.
	 *
	 * @param focalPoint the focal point
	 */
	public void setFocalPoint(Vec3 focalPoint) {
		this.focalPoint = focalPoint;
	}

	/**
	 * Gets thrust.
	 *
	 * @return the thrust
	 */
	public Vec3 getThrust() {
		return thrust;
	}

	/**
	 * Sets thrust.
	 *
	 * @param thrust the thrust
	 */
	public void setThrust(Vec3 thrust) {
		this.thrust = thrust;
	}

	/**
	 * Gets eye.
	 *
	 * @return the eye
	 */
	public Vec3 getEye() {
		return eye;
	}

	/**
	 * Gets cam basis. 1st column is the x-axis, 2nd is the y-axis and 3rd is the z-axis
	 *
	 * @return the cam basis
	 */
	public Matrix getCamBasis() {
		return camBasis;
	}

	/**
	 * Gets inverse cam basis.
	 *
	 * @return the inverse cam basis
	 */
	public Matrix getInverseCamBasis() {
		return inverseCamBasis;
	}
}
