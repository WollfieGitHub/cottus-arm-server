package fr.wollfie.cottus.models.arm.positioning.articulations;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.wollfie.cottus.dto.Articulation;

public class DrivenArticulation extends ArticulationImpl {
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                                       FIELDS                                         ||
// ||                                                                                      ||
// \\======================================================================================//
    
    @JsonProperty("target") private Articulation target;
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
    
    public DrivenArticulation(
            @JsonProperty("target") Articulation target,
            @JsonProperty("parent") DrivenArticulation parent,
            @JsonProperty("stepSize") double stepSize
    ) {
        super(String.format("%s_wrapper", target.getName()), parent, target.getAxis(), target.getLength());
        this.target = target;
        this.stepSize = stepSize;
    }
    
    @Override
    public void update() {
        // Update the angle by making it smoothly change towards the target
        this.angleRad = stepSize * target.getAngleRad() + (1-stepSize) * this.angleRad;
        // Then update the transform
        super.update();
    }

//=========   ====  == =
//      OVERRIDE PROPERTIES
//=========   ====  == =
    
    @Override
    public Articulation getParent() { return null; }
    
}
