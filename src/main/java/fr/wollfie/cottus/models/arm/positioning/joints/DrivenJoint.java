package fr.wollfie.cottus.models.arm.positioning.joints;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.dto.JointBounds;
import fr.wollfie.cottus.dto.JointTransform;

public class DrivenJoint extends JointImpl {
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                                       FIELDS                                         ||
// ||                                                                                      ||
// \\======================================================================================//
    
    @JsonProperty("target") private Joint target;
    @JsonProperty("stepSize") private double stepSize;
    
//=========   ====  == =
//      ANGLE RAD PROPERTY
//=========   ====  == =
    
    @Override
    public void setAngleRad(double angleRad) { throw new UnsupportedOperationException("This articulation is" +
            " driven and its angle cannot be directly controlled, please change its dependency's angle" +
            " instead"); }

// //======================================================================================\\
// ||                                                                                      ||
// ||                                       CONSTRUCTOR                                    ||
// ||                                                                                      ||
// \\======================================================================================//
    
    public DrivenJoint(
            @JsonProperty("target") Joint target,
            @JsonProperty("parent") DrivenJoint parent,
            JointBounds bounds,
            JointTransform transform,
            @JsonProperty("stepSize") double stepSize
    ) {
        super(String.format("%s_wrapper", target.getName()), parent, bounds, transform, target.isVirtual());
        this.target = target;
        this.stepSize = stepSize;
    }

    /**
     * Updates the position and rotation of the articulation, by shadowing the articulation
     * it has for target
     */
    public void update() {
        // Update the angle by making it smoothly change towards the target
        double angleRad = getAngleRad();
        this.setAngleRad(stepSize * target.getAngleRad() + (1-stepSize) * angleRad);
    }

//=========   ====  == =
//      OVERRIDE PROPERTIES
//=========   ====  == =
    
    @Override
    public Joint getParent() { return null; }
    
}
