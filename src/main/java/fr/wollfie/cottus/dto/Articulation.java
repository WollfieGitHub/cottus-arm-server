package fr.wollfie.cottus.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import fr.wollfie.cottus.models.arm.positioning.Transform;
import fr.wollfie.cottus.utils.maths.Axis3D;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

public interface Articulation {
    
//=========   ====  == =
//      INFO PROPERTIES
//=========   ====  == =
    
    /** @return The length in mm of the articulation between the rotation point and the rotation point
     * of its child
     * */
    @JsonGetter("length")
    double getLength();
    
    /** @return The name of the articulation */
    @JsonGetter("name")
    String getName();
    
    /** @return The transform of the articulation */
    @JsonGetter("transform")
    Transform getTransform();
    
//=========   ====  == =
//      PARENT PROPERTY
//=========   ====  == =
    
    /** @return The parent articulation of {@code null} if the 
     * articulation is the root */
    @JsonGetter("parent")
    Articulation getParent();
    
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
    
}
