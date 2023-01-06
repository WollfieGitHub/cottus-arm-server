package fr.wollfie.cottus.utils.maths;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.wollfie.cottus.utils.Preconditions;
import fr.wollfie.cottus.utils.Utils;
import org.ejml.simple.SimpleMatrix;

import static fr.wollfie.cottus.utils.maths.Axis3D.*;
import static java.lang.Math.*;

public class Vector3D extends Vector {
    /** The zero vector */
    public static Vector3D Zero =  Vector3D.of(0, 0, 0);

    @JsonProperty("x") public final double x;
    @JsonProperty("y") public final double y;
    @JsonProperty("z") public final double z;

    Vector3D(
        @JsonProperty("x") double x,
        @JsonProperty("y") double y,
        @JsonProperty("z") double z
    ) {
        super(x,y,z);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /** @return A new vector with the specified (x, y, z) coordinates */
    public static Vector3D of(double x, double y, double z) {
        return new Vector3D(x, y, z);
    }

    /** @return True if the vector is zero, false otherwise */
    @Override
    public boolean isZero() {
        return Utils.isZero(x)
                && Utils.isZero(y)
                && Utils.isZero(z);
    }
    
    /**
     * Add that vector to this vector
     * @param that The other vector to add
     * @return the result of the sum between the two vectors
     */
    @Override
    public Vector3D plus(Vector that) {
        Preconditions.checkArgument(that.dim == 3);
        return Vector3D.of(
                this.x + that.get(0),
                this.y + that.get(1),
                this.z + that.get(2)
        );
    }

    /** @return The result of the multiplication of this vector by the specified scalar */
    @Override
    public Vector3D scaled(double scalar) {
        return Vector3D.of(scalar * this.x, scalar * this.y, scalar * this.z);
    }
    
    /** @return The result of the subtraction of {@code that} vector to {@code this} vector */
    @Override
    public Vector3D minus(Vector that) {
        return this.plus(that.scaled(-1));
    }

    /** @return The result of the dot product between {@code this} vector and {@code that} vector */
    public double dot(Vector3D that) {
        return this.x*that.x + this.y*that.y + this.z*that.z;
    }
    
    /** @return The result fo the cross prodcut between {@code this} vector and {@code that} vector */
    public Vector3D cross(Vector3D that) {
        return Vector3D.of(
                this.y * that.z - this.z * that.y,
                this.z * that.x - this.x * that.z,
                this.x * that.y - this.y * that.x
        );
    }

    /** @return The norm of this vector, squared */
    @Override
    public double normSquared() { return this.dot(this); }

    /** @return The norm of this vector */
    @Override
    public double norm() { return sqrt(this.normSquared()); }

    /**
     * @return This vector, normalized
     * @throws UnsupportedOperationException If this vector is zero and cannot be normalized
     */
    @Override
    public Vector3D normalized() throws UnsupportedOperationException {
        if (isZero()) { throw new UnsupportedOperationException("The vector is zero and cannot be normalized !"); }
        double norm = this.norm();
        return Vector3D.of(x/norm, y/norm, z/norm);
    }

    /**
     * Rotates the vector at the origin using the specified angle around the given axis
     * @param axis The axis to rotate the vector around
     * @param rotRad The angle by which to rotate the vector
     * @return A new vector rotated around the origin by {@code rotRad} around the given axis 
     */
    public Vector3D rotatedAtOriginAround(Axis3D axis, double rotRad) {
        return switch (axis) {
            case X -> Vector3D.of(x, y * cos(rotRad) - z * sin(rotRad), y * sin(rotRad) + z * cos(rotRad));
            case Y -> Vector3D.of(x * cos(rotRad) + z * sin(rotRad), y, -x * sin(rotRad) + z * cos(rotRad));
            case Z -> Vector3D.of(x * cos(rotRad) - y * sin(rotRad), x * sin(rotRad) + y * cos(rotRad), z);
        };
    }

    /**
     * Rotates the vector at the specified point using the specified angle around the given axis
     * @param axis The axis to rotate the vector around
     * @param rotRad The angle by which to rotate the vector
     * @param rotationPoint The point around which the rotation occurs
     * @return A new vector rotated around {@code rotationPoint} by {@code rotRad} around the given axis 
     */
    public Vector3D rotatedAtPointAround(Axis3D axis, double rotRad, Vector3D rotationPoint) {
        return this.minus(rotationPoint)
                .rotatedAtOriginAround(axis, rotRad)
                .plus(rotationPoint);
    }

    /**
     * Rotates the vector at the origin using the specified angles
     * @param eulerAngles The euler angles to use to rotate the vector
     * @return A new vector rotated around the origin by the given euler angles
     */
    public Vector3D rotatedAtOriginUsing(Vector3D eulerAngles) {
        return this.rotatedAtOriginAround(X, eulerAngles.x)
                .rotatedAtOriginAround(Axis3D.Y, eulerAngles.y)
                .rotatedAtOriginAround(Axis3D.Z, eulerAngles.z);
    }

    /**
     * Inverse the rotation of the vector at the origin using the specified angles
     * @param eulerAngles The euler angles to use to inverse the rotation the vector
     * @return A new vector rotated around the origin using the given euler angles
     * such that <pre>{@code
     * initialVector
     *      .rotatedAtOriginUsing(angles)
     *      .rotatedInverseAtOriginUsing(angles)
     * == initialVector
     * }</pre>
     */
    public Vector3D rotatedInverseAtOriginUsing(Vector3D eulerAngles) {
        return this.rotatedAtOriginAround(Axis3D.Z, -eulerAngles.z)
                .rotatedAtOriginAround(Axis3D.Y, -eulerAngles.y)
                .rotatedAtOriginAround(X, -eulerAngles.x);
    }

    /**
     * Inverse the rotation of the vector at the specified point using the specified angles
     * @param eulerAngles The euler angles to use to inverse the rotation the vector
     * @param rotationPoint The point around which the rotation occurs
     * @return A new vector rotated around {@code rotationPoint} using the given euler angles
     * such that <pre>{@code
     * initialVector
     *      .rotatedAtPointUsing(angles, point)
     *      .rotatedInverseAtPointUsing(angles, point)
     * == initialVector
     * }</pre>
     */
    public Vector3D rotatedInverseAtPointUsing(Vector3D eulerAngles, Vector3D rotationPoint) {
        return this.minus(rotationPoint)
                .rotatedInverseAtOriginUsing(eulerAngles)
                .plus(rotationPoint);
    }

    /**
     * Rotates the vector at the specified point using the specified angles
     * @param eulerAngles The euler angles to use to rotate the vector
     * @param rotationPoint The point around which the rotation occurs
     * @return A new vector rotated around {@code rotationPoint} by the given euler angles
     */
    public Vector3D rotatedAtPointUsing(Vector3D eulerAngles, Vector3D rotationPoint) {
        return this.minus(rotationPoint)
                .rotatedAtOriginUsing(eulerAngles)
                .plus(rotationPoint);
    }

    /**
     * Sets the value of the coordinate corresponding to the given axis to {@code val}
     * @param axis The axis used to change the coordinate
     * @param val The new value to set
     * @return The same vector but with one of its coordinates set to {@code val}
     */
    public Vector3D withAxis3DSetTo(Axis3D axis, double val) {
        return switch (axis) {
            case X -> Vector3D.of(val, y, z);
            case Y -> Vector3D.of(x, val, z);
            case Z -> Vector3D.of(x, y, val);
        };
    }
    
    /** @return The angle in radians between {@code this} vector and {@code that} vector */
    public double angleTo(Vector3D that) {
        return acos(this.normalized().dot(that.normalized()));
    }

    /** @return The distance in mm between {@code this} vector and {@code that} vector */
    public double distanceTo(Vector3D that) { return this.minus(that).norm(); }

    @Override
    public String toString() { return String.format("Vector3D{%5.3f, %5.3f, %5.3f}", x, y, z); }

    /**
     * Return the result of {@code ratio} of {@code this} vector and {@code (1-ratio)} of {@code that}
     * vector
     * @param that The other vector
     * @param ratio The ratio of {@code this} vector to preserve
     * @return The new interpolated vector
     */
    public Vector3D interpolate(Vector3D that, double ratio) {
        return this.scaled(ratio).plus(that.scaled(1-ratio));
    }
    
    /** @return The skew-symmetric matrix of the vector */
    public SimpleMatrix skewSymmetric() {
        double[][] values = new double[][] {
                new double[] {  0, -z,  y, },
                new double[] {  z,  0, -x, },
                new double[] { -y,  x,  0, },
        };
        return new SimpleMatrix(values);
    }
    
    /** @return The radius of this vector in the cylindrical coordinate system */
    public double getRadius() { return sqrt(x*x + y*y); }
    
    /** @return The angle of this vector in the cylindrical coordinate system */
    public double getTheta() { return atan2(y,x); }
}
