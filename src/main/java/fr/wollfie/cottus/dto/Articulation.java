package fr.wollfie.cottus.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import fr.wollfie.cottus.utils.maths.Axis3D;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

public interface Articulation {
    
//=========   ====  == =
//      INFO PROPERTIES
//=========   ====  == =
    
    /** @return The length in mm of the articulation */
    @JsonGetter("length")
    double getLength();
    
    /** @return The name of the articulation */
    @JsonGetter("name")
    String getName();
    
    /** @return The axis along which the articulation rotates */
    @JsonGetter("axis")
    Axis3D getAxis();
    
//=========   ====  == =
//      PARENT PROPERTY
//=========   ====  == =
    
    /** @return The parent articulation of {@code null} if the 
     * articulation is the root */
    @JsonGetter("parent")
    Articulation getParent();
    
//=========   ====  == =
//      POSITION CONTROL
//=========   ====  == =
    
    /** @return The global position of the articulation,
     * relative to the world's origin */
    @JsonGetter("globalPosition")
    Vector3D getGlobalPosition();
    
    /** @return The local position of the articulation, 
     * relative to the articulation before it */
    @JsonGetter("localPosition")
    Vector3D getLocalPosition();

    /** @return The global euler angles of the articulation,
     * relative to the world's origin */
    @JsonGetter("globalRotation")
    Rotation getGlobalRotation();
    
    /** @return The local euler angles of the articulation,
     * relative to the articulation before it */
    @JsonGetter("localRotation")
    Rotation getLocalRotation();
    
//=========   ====  == =
//      ROTATION CONTROL
//=========   ====  == =
    
    /** 
     * Set the rotation of this articulation to the specified angle
     * @param angleRad The angle in radians to which the articulation should rotate
     */
    @JsonSetter("angleRad")
    void setAngleRad(double angleRad);
    
    /** @return The angle in radians to which the articulation is rotated */
    @JsonGetter("angleRad")
    double getAngleRad();
    
    /** If the articulation needs to do some kind of actualization after
     * its angle has been set */
    void update();
    
}
