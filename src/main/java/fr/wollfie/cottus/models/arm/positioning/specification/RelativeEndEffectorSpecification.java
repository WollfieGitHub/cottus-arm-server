package fr.wollfie.cottus.models.arm.positioning.specification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.specification.EndEffectorSpecification;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.quarkus.logging.Log;

import java.util.List;

/**
 * Specification for the End effector, in the end effector's referential
 */
public class RelativeEndEffectorSpecification extends EndEffectorSpecification {

    /** True if this specification is the root for others, meaning it should be considered,
     *  once its position is evaluated, as 0 for all following {@link RelativeEndEffectorSpecification}
     *  that are not root*/
    private EndEffectorSpecification root;
    public EndEffectorSpecification getRoot() { return root; }

    /**
     * Builds a new Relative End effector specification
     * @param root The {@link AbsoluteEndEffectorSpecification} this specification is relative to. If left
     *             {@code null}, then the specification is relative to the arm's absolute specification
     *             at the moment the {@link RelativeEndEffectorSpecification#fixAsAbsolute(CottusArm)} is called
     * @param endEffectorPosition The position difference of the end effector
     * @param endEffectorOrientation The orientation difference of the end effector
     * @param preferredArmAngle The preferred arm angle difference
     */
    @JsonCreator
    private RelativeEndEffectorSpecification(
            @JsonProperty("root") EndEffectorSpecification root,
            @JsonProperty("endEffectorPosition") Vector3D endEffectorPosition,
            @JsonProperty("endEffectorRotation") Rotation endEffectorOrientation,
            @JsonProperty("armAngle") double preferredArmAngle
    ) {
        super(endEffectorPosition, endEffectorOrientation, preferredArmAngle);
        this.root = root;
    }

    /**
     * Builds a new Relative End effector specification
     * @param rootSpecification The {@link AbsoluteEndEffectorSpecification} this specification is relative to.
     * @param endEffectorPosition The position difference of the end effector
     * @param endEffectorOrientation The orientation difference of the end effector
     * @param preferredArmAngle The preferred arm angle difference
     */
    public static RelativeEndEffectorSpecification createRelativeTo(
            EndEffectorSpecification rootSpecification,
            Vector3D endEffectorPosition,
            Rotation endEffectorOrientation,
            double preferredArmAngle
    ) {
        return new RelativeEndEffectorSpecification(rootSpecification, endEffectorPosition, endEffectorOrientation, preferredArmAngle);
    }

    /**
     * Builds a new Relative End effector specification. The specification is relative to the arm's absolute specification
     * at the moment the {@link RelativeEndEffectorSpecification#fixAsAbsolute(CottusArm)} is called
     * @param endEffectorPosition The position difference of the end effector
     * @param endEffectorOrientation The orientation difference of the end effector
     * @param preferredArmAngle The preferred arm angle difference
     */
    public static RelativeEndEffectorSpecification createRelativeToActive(
            Vector3D endEffectorPosition,
            Rotation endEffectorOrientation,
            double preferredArmAngle
    ) {
        return new RelativeEndEffectorSpecification(null, endEffectorPosition, endEffectorOrientation, preferredArmAngle);
    }
    

    @Override
    public List<Double> getAnglesFor(CottusArm cottusArm) throws NoSolutionException {
        return this.fixAsAbsolute(cottusArm).getAnglesFor(cottusArm);
    }

    /**
     * Convert the relative specification to an absolute specification given 
     * the context of the current arm's state
     * @param arm The arm's state
     * @return The absolute specification for the current context
     */
    public AbsoluteEndEffectorSpecification fixAsAbsolute(CottusArm arm) {
        if (root == null) { root = arm.getEndEffectorSpecification(); }
        else if (root instanceof RelativeEndEffectorSpecification rel) { root = rel.fixAsAbsolute(arm); }
        
        return new AbsoluteEndEffectorSpecification(
                root.getEndEffectorPosition().plus( this.getEndEffectorPosition() ),
                root.getEndEffectorOrientation().plus( this.getEndEffectorOrientation() ),
                root.getPreferredArmAngle() + this.getPreferredArmAngle()
        );
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
