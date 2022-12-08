package fr.wollfie.cottus.models.arm.positioning;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import fr.wollfie.cottus.utils.maths.Vector3D;
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
    public Vector3D getGlobalPosition() {
        if (parent == null) { return getLocalPosition(); }
        
        Vector3D parentGlobalPosition = parent.getGlobalPosition();
        return getLocalPosition()
                .rotatedAtPointUsing(
                        parent.getGlobalRotation().getEulerAngles(),
                        parentGlobalPosition
                ).add(parentGlobalPosition);
    }

    /** Sets the global position of this transform */
    @JsonSetter("globalPosition")
    public void setGlobalPosition(Vector3D globalPosition) {
        if (parent == null) { this.localPosition = globalPosition; }
        else { this.localPosition = globalPosition.subtract(parent.getGlobalPosition()); }
    }
    
}
