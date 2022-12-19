package fr.wollfie.cottus.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import fr.wollfie.cottus.utils.maths.Axis3D;
import fr.wollfie.cottus.utils.maths.Vector3D;

/** An object that transforms a point from a global space to the local space it represents */
public interface JointTransform {

    /**
     * Transforms a vector from the local space to  a vector in global space
     * @param localPosition The vector from local space
     * @return The vector in global space
     */
    Vector3D transform(Vector3D localPosition);

    /**
     * Transforms a vector from the global space to a vector in local space
     * @param globalPosition The vector from global space
     * @return The vector in local space
     */
    Vector3D inverseTransform(Vector3D globalPosition);
    
//=========   ====  == =
//      LOCAL REFERENTIAL
//=========   ====  == =
    
    /** @return The origin of the transform's frame of reference */
    @JsonGetter("origin")
    default Vector3D getOrigin() { return transform(Vector3D.Zero); }
    
    /** @return The x unit vector projected into the transform's local space*/
    @JsonGetter("localX")
    default Vector3D getX() { return transform(Axis3D.X.unitVector); }
    
    /** @return The x unit vector projected into the transform's local space*/
    @JsonGetter("localY")
    default Vector3D getY() { return transform(Axis3D.Y.unitVector); }
    
    /** @return The x unit vector projected into the transform's local space*/
    @JsonGetter("localZ")
    default Vector3D getZ() { return transform(Axis3D.Z.unitVector); }
    
//=========   ====  == =
//      JOINT CONTROL
//=========   ====  == =
    
    /** Sets the angle of rotation of the joint's transform */
    void setAngle(double angleRad);
    
    /** @return The angle of rotation of the joint's transform  in radians*/
    double getAngle();
}
