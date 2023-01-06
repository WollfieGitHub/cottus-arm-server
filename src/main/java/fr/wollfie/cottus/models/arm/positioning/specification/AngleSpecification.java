package fr.wollfie.cottus.models.arm.positioning.specification;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.specification.ArmSpecification;

import java.util.List;

/**
 * This specification is the default one, it just specifies every angles
 * for the arm's articulations
 */
public class AngleSpecification implements ArmSpecification {
    
    private final List<Double> angles;

    public AngleSpecification(
            @JsonProperty("a0") double a0,
            @JsonProperty("a1") double a1,
            @JsonProperty("a2") double a2,
            @JsonProperty("a3") double a3,
            @JsonProperty("a4") double a4,
            @JsonProperty("a5") double a5,
            @JsonProperty("a6") double a6
    ) { this.angles = List.of(a0, a1, a2, a3, a4, a5, a6); }

    public AngleSpecification(List<Double> angles) { this.angles = angles; }
    
    @Override
    public List<Double> getAnglesFor(CottusArm cottusArm) { return this.angles; }
}
