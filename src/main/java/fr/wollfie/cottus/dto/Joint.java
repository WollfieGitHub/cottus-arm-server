package fr.wollfie.cottus.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import fr.wollfie.cottus.exception.AngleOutOfBoundsException;

public interface Joint {
    
//=========   ====  == =
//      INFO PROPERTIES
//=========   ====  == =
    
    /** @return The name of the articulation */
    @JsonGetter("name")
    String getName();
    
    /** @return The transform of the articulation */
    @JsonGetter("transform")
    JointTransform getTransform();
    
    /** @return True if this joint only exist of the need of the modelisation by the 
     * DH parameters */
    @JsonGetter("virtual")
    boolean isVirtual();
    
//=========   ====  == =
//      PARENT PROPERTY
//=========   ====  == =
    
    /** @return The parent articulation of {@code null} if the 
     * articulation is the root */
    @JsonGetter("parent")
    Joint getParent();
    
//=========   ====  == =
//      ROTATION CONTROL
//=========   ====  == =
    
    /** @return The bounds for the angle of the joint */
    @JsonGetter("bounds")
    JointBounds getBounds();
    
    /** 
     * Set the rotation of this articulation to the specified angle
     * @param angleRad The angle in radians to which the articulation should rotate
     */
    @JsonSetter("angleRad")
    void setAngleRad(double angleRad) throws AngleOutOfBoundsException;
    
    /** @return The angle in radians to which the articulation is rotated */
    @JsonGetter("angleRad")
    double getAngleRad();
    
}
