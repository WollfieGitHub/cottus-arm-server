package fr.wollfie.cottus.utils.maths;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum Axis3D {
    
    X(Vector3D.of(1, 0, 0)),
    Y(Vector3D.of(0, 1, 0)),
    Z(Vector3D.of(0, 0, 1)),
    ;
    
    public final Vector3D unitVector;

    Axis3D(Vector3D unitVector) { this.unitVector = unitVector; }

    public boolean isZero() {
        return unitVector.isZero();
    }

    public Vector3D add(Vector3D that) {
        return unitVector.add(that);
    }

    public Vector3D scaledBy(double scalar) { return unitVector.scaledBy(scalar); }
    public Vector3D subtract(Vector3D that) { return unitVector.subtract(that); }
    public double dot(Vector3D that) { return unitVector.dot(that); }
    public Vector3D cross(Vector3D that) { return unitVector.cross(that); }
    public double normSquared() { return unitVector.normSquared(); } 
    public double norm() { return unitVector.norm(); }
    public Vector3D normalized() throws UnsupportedOperationException { return unitVector.normalized(); }
    public Vector3D rotatedAtOriginAround(Axis3D axis, double rotRad) { return unitVector.rotatedAtOriginAround(axis, rotRad); }
    public Vector3D rotatedAtPointAround(Axis3D axis, double rotRad, Vector3D rotationPoint) { return unitVector.rotatedAtPointAround(axis, rotRad, rotationPoint); }
    public Vector3D rotatedAtOriginUsing(Vector3D eulerAngles) { return unitVector.rotatedAtOriginUsing(eulerAngles); }
    public Vector3D rotatedInverseAtOriginUsing(Vector3D eulerAngles) { return unitVector.rotatedInverseAtOriginUsing(eulerAngles); }
    public Vector3D rotatedInverseAtPointUsing(Vector3D eulerAngles, Vector3D rotationPoint) { return unitVector.rotatedInverseAtPointUsing(eulerAngles, rotationPoint); }
    public Vector3D rotatedAtPointUsing(Vector3D eulerAngles, Vector3D rotationPoint) { return unitVector.rotatedAtPointUsing(eulerAngles, rotationPoint); }
    public Vector3D withAxis3DSetTo(Axis3D axis, double val) { return unitVector.withAxis3DSetTo(axis, val); }
    public double angleTo(Vector3D that) { return unitVector.angleTo(that); }
    public double distanceTo(Vector3D that) { return unitVector.distanceTo(that); }
}
