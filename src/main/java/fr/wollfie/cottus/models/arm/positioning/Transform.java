package fr.wollfie.cottus.models.arm.positioning;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import fr.wollfie.cottus.utils.maths.Axis3D;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.matrices.Matrix;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.smallrye.common.constraint.NotNull;
import org.jboss.resteasy.reactive.common.NotImplementedYet;

public class Transform {
    
//=========   ====  == =
//      PARENT PROPERTY
//=========   ====  == =
    
    /** The parent transform this transform depends on.
     * Can be null, in which case its position is interpreted as the one relative
     * to the world's origin */
    @JsonProperty("parent") public final Transform parent;

//=========   ====  == =
//      CONSTRUCTORS 
//=========   ====  == =
    
    private Transform(
            @JsonProperty("parent") Transform parent
    ) { this.parent = parent; }
    
    /** @return A new transform with the specified parent, i.e., its local position
     * is relative to the given parent */
    public static Transform createFrom(@NotNull Transform parent) { return new Transform(parent); }
    
    /** @return A new transform with parent set to null, i.e., its local position is
     * relative to the world's origin */
    public static Transform createFromRoot() { return new Transform(null); }

    /**
     * Use the given transformation matrix to position this transform 
     * @param parentToChild The transformation matrix which, from a point in {@code this} local space
     *                     will transform the point in the child's local space
     */
    public void setTransform(Matrix parentToChild) {
        this.localPosition = parentToChild.multipliedBy(Vector3D.Zero());
        double thetaX = Axis3D.X.getUnitVector().angleTo(parentToChild.multipliedBy(Axis3D.X.getUnitVector()));
        double thetaY = Axis3D.Y.getUnitVector().angleTo(parentToChild.multipliedBy(Axis3D.Y.getUnitVector()));
        double thetaZ = Axis3D.Z.getUnitVector().angleTo(parentToChild.multipliedBy(Axis3D.Z.getUnitVector()));
        this.localRotation = Rotation.from(Vector3D.of(thetaX, thetaY, thetaZ));
    }

//=========   ====  == =
//      ROTATION PROPERTY
//=========   ====  == =
    
    /** Intrinsic rotation of the transform saved as Euler Angles */
    @JsonProperty("localRotation")
    private Rotation localRotation;
    
    /** @return The local intrinsic rotation of the transform relative to its parent */
    @JsonGetter("localRotation")
    public Rotation getLocalRotation() { return this.localRotation; }
    
    /** @return The global rotation of the transform relative World's origin */
    @JsonGetter("globalRotation")
    public Rotation getGlobalRotation() { 
        return parent == null 
                ? getLocalRotation()
                : Rotation.from(getLocalRotation()
                                .getEulerAngles()
                                .add(parent.getGlobalRotation().getEulerAngles()));
    }

    /** Sets the local rotation of this transform */
    @JsonSetter("localRotation")
    public void setLocalRotation(Rotation localRotation) { this.localRotation = localRotation; }
    
    public void setLocalRotation(Axis3D axis, double value) {
        setLocalRotation(Rotation.from(getLocalRotation().getEulerAngles().withAxis3DSetTo(axis, value)));    
    }

    @JsonSetter("globalRotation")
    public void setGlobalRotation(Rotation globalRotation) { throw new NotImplementedYet(); }
    
//=========   ====  == =
//      POSITION PROPERTY
//=========   ====  == =
    
    /** Local position compared to the World's origin */
    @JsonProperty("localPosition")
    private Vector3D localPosition;

    /** Sets the local position of this transform */
    @JsonSetter("localPosition")
    public void setLocalPosition(Vector3D localPosition) { this.localPosition = localPosition; }

    /** @return The local position of the transform, relative to its parent */
    @JsonGetter("localPosition")
    public Vector3D getLocalPosition() {
        return this.localPosition;
    }
    
    /** @return The local position of the transform  */
    @JsonGetter("globalPosition")
    public Vector3D getGlobalPosition() { return this.transform(getLocalPosition()); }

    /** Sets the global position of this transform */
    @JsonSetter("globalPosition")
    public void setGlobalPosition(Vector3D globalPosition) {
        this.setLocalPosition(this.inverseTransform(globalPosition));
    }
    
//=========   ====  == =
//      TRANSFORM FUNCTIONS
//=========   ====  == =

    /**
     * Transforms a position vector that would be in the local space of this transform to
     * a position vector in world space
     * @param localPosition The local position
     * @return The world space position
     */
    public Vector3D transform(Vector3D localPosition) {
        if (parent == null) { return localPosition; }

        Vector3D parentGlobalPosition = parent.getGlobalPosition();
        return localPosition.add(parentGlobalPosition).rotatedAtPointUsing(
                parent.getGlobalRotation().getEulerAngles(), 
                parentGlobalPosition
        );
    }

    /**
     * Transforms a position vector that would be in the world space of this transform to
     * a position vector in local space
     * @param globalPosition The global position
     * @return The world space position
     */
    public Vector3D inverseTransform(Vector3D globalPosition) {
        if (parent == null) { return globalPosition; }

        Vector3D parentGlobalPosition = parent.getGlobalPosition();
        return globalPosition.rotatedInverseAtPointUsing(
                parent.getGlobalRotation().getEulerAngles().scaledBy(-1),
                parentGlobalPosition
        ).subtract(parentGlobalPosition);
    }
    
//=========   ====  == =
//      AXIS PROPERTY
//=========   ====  == =

    /**
     * Converts the position of the local axis to 
     * @param axis The local axis to convert to world space
     * @return The local axis in world space
     */
    public Vector3D getLocalAxis(Axis3D axis) {
        return this.transform(axis.getUnitVector());
    }
    
}
