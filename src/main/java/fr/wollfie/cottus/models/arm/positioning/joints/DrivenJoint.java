package fr.wollfie.cottus.models.arm.positioning.joints;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.dto.JointBounds;
import fr.wollfie.cottus.dto.JointTransform;

public class DrivenJoint extends JointImpl {
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                                       CONSTRUCTOR                                    ||
// ||                                                                                      ||
// \\======================================================================================//
    
    public DrivenJoint(
            @JsonProperty("target") Joint target,
            @JsonProperty("parent") DrivenJoint parent,
            JointBounds bounds,
            JointTransform transform
    ) {
        super(String.format("%s_wrapper", target.getName()), parent, bounds, transform, target.isVirtual());
    }

}
