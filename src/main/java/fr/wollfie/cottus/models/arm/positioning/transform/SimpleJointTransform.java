package fr.wollfie.cottus.models.arm.positioning.transform;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import fr.wollfie.cottus.dto.JointTransform;
import fr.wollfie.cottus.utils.maths.Axis3D;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.matrices.MatrixUtil;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.smallrye.common.constraint.NotNull;
import org.ejml.simple.SimpleMatrix;
import org.jboss.resteasy.reactive.common.NotImplementedYet;

public class SimpleJointTransform implements JointTransform {
    
//=========   ====  == =
//      PARENT PROPERTY
//=========   ====  == =
    
    /** The parent transform this transform depends on.
     * Can be null, in which case its position is interpreted as the one relative
     * to the world's origin */
    @JsonProperty("parent") public final SimpleJointTransform parent;

//=========   ====  == =
//      CONSTRUCTORS 
//=========   ====  == =
    
    private SimpleJointTransform(@JsonProperty("parent") SimpleJointTransform parent) { this.parent = parent; }
    
    /** @return A new transform with the specified parent, i.e., its local position
     * is relative to the given parent */
    public static SimpleJointTransform createFrom(@NotNull SimpleJointTransform parent) { return new SimpleJointTransform(parent); }
    
    /** @return A new transform with parent set to null, i.e., its local position is
     * relative to the world's origin */
    public static SimpleJointTransform createFromRoot() { return new SimpleJointTransform(null); }

    /**
     * Use the given transformation matrix to position this transform 
     * @param parentToChild The transformation matrix which, from a point in {@code this} local space
     *                     will transform the point in the child's local space
     */
    public void setTransform(SimpleMatrix parentToChild) {
        this.localPosition = MatrixUtil.extractTranslation(parentToChild);
        this.localRotation = Rotation.from(MatrixUtil.extractRotation(parentToChild));
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
                                .plus(parent.getGlobalRotation().getEulerAngles()));
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

        Vector3D parentGlobalPosition = parent.getGlobalPosition() ;
        return localPosition.rotatedAtOriginUsing(parent.getGlobalRotation().getEulerAngles())
                .plus(parentGlobalPosition);
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
        return globalPosition.minus(parentGlobalPosition).rotatedInverseAtOriginUsing(
                parent.getGlobalRotation().getEulerAngles()
        );
    }

    @Override
    public void setAngle(double angleRad) {
        setLocalRotation(Rotation.from(Axis3D.Z.scaledBy(angleRad)));
    }

    @Override
    public double getAngle() {
        return getLocalRotation().getEulerAngles().z;
    }

//=========   ====  == =
//      AXIS PROPERTY
//=========   ====  == =

    /**
     * Converts the position of the local axis to 
     * @param axis The local axis to convert to world space
     * @return The local axis in world space
     */
    public Vector3D getLocalAxis(Axis3D axis) { return this.transform(axis.unitVector); }
    
}
