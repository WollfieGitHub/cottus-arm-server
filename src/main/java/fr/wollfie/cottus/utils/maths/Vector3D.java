package fr.wollfie.cottus.utils.maths;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.wollfie.cottus.utils.Constants;
import fr.wollfie.cottus.utils.Utils;

import static fr.wollfie.cottus.utils.maths.Axis3D.*;
import static java.lang.Math.*;

public class Vector3D {
    
    @JsonProperty("x") public final double x;
    @JsonProperty("y") public final double y;
    @JsonProperty("z") public final double z;
    
    Vector3D(
        @JsonProperty("x") double x,
        @JsonProperty("y") double y,
        @JsonProperty("z") double z
    ) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /** @return A new vector with the specified (x, y, z) coordinates */
    public static Vector3D of(double x, double y, double z) {
        return new Vector3D(x, y, z);
    }

    /** @return The zero vector */
    public static Vector3D Zero() {
        return Vector3D.of(0, 0, 0);
    }

    /** @return True if the vector is zero, false otherwise */
    @JsonIgnore
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
    public Vector3D add(Vector3D that) {
        return Vector3D.of(
                this.x + that.x,
                this.y + that.y,
                this.z + that.z
        );
    }

    /** @return The result of the multiplication of this vector by the specified scalar */
    public Vector3D scaledBy(double scalar) {
        return Vector3D.of(scalar * this.x, scalar * this.y, scalar * this.z);
    }
    
    /** @return The result of the subtraction of {@code that} vector to {@code this} vector */
    public Vector3D subtract(Vector3D that) {
        return this.add(that.scaledBy(-1));
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
    public double normSquared() { return this.dot(this); }

    /** @return The norm of this vector */
    public double norm() { return sqrt(this.normSquared()); }

    /**
     * @return This vector, normalized
     * @throws UnsupportedOperationException If this vector is zero and cannot be normalized
     */
    public Vector3D normalized() throws UnsupportedOperationException {
        if (isZero()) { throw new UnsupportedOperationException("The vector is zero and cannot be normalized !"); }
        double norm = this.norm();
        return Vector3D.of(x/norm, y/norm, z/norm);
    }

    
    public Vector3D rotatedAtOriginAround(Axis3D axis, double rotRad) {
        return switch (axis) {
            case X -> Vector3D.of(x, y * cos(rotRad) - z * sin(rotRad), y * sin(rotRad) + z * cos(rotRad));
            case Y -> Vector3D.of(x * cos(rotRad) + z * sin(rotRad), y, -x * sin(rotRad) + z * cos(rotRad));
            case Z -> Vector3D.of(x * cos(rotRad) - y * sin(rotRad), x * sin(rotRad) + y * cos(rotRad), z);
        };
    }

    public Vector3D rotatedAtPointAround(Axis3D axis, double rotRad, Vector3D rotationPoint) {
        return this.subtract(rotationPoint)
                .rotatedAtOriginAround(axis, rotRad)
                .add(rotationPoint);
    }

    public Vector3D rotatedAtOriginUsing(Vector3D eulerAngles) {
        return this.rotatedAtOriginAround(X, eulerAngles.x)
                .rotatedAtOriginAround(Axis3D.Y, eulerAngles.y)
                .rotatedAtOriginAround(Axis3D.Z, eulerAngles.z);
    }

    public Vector3D rotatedInverseAtOriginUsing(Vector3D eulerAngles) {
        return this.rotatedAtOriginAround(Axis3D.Z, -eulerAngles.z)
                .rotatedAtOriginAround(Axis3D.Y, -eulerAngles.y)
                .rotatedAtOriginAround(X, -eulerAngles.x);
    }

    public Vector3D rotatedInverseAtPointUsing(Vector3D eulerAngles, Vector3D rotationPoint) {
        return this.subtract(rotationPoint)
                .rotatedInverseAtOriginUsing(eulerAngles)
                .add(rotationPoint);
    }

    public Vector3D rotatedAtPointUsing(Vector3D eulerAngles, Vector3D rotationPoint) {
        return this.subtract(rotationPoint)
                .rotatedAtOriginUsing(eulerAngles)
                .add(rotationPoint);
    }

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
    public double distanceTo(Vector3D that) { return this.subtract(that).norm(); }

    @Override
    public String toString() {
        return "Vector3D{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
