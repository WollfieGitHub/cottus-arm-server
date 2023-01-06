package fr.wollfie.cottus.models.arm.positioning.specification;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.specification.EndEffectorSpecification;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

import java.util.List;

public class RelativeEndEffectorSpecification extends EndEffectorSpecification {

    /** True if this specification is the root for others, meaning it should be considered,
     *  once its position is evaluated, as 0 for all following {@link RelativeEndEffectorSpecification}
     *  that are not root*/
    private final AbsoluteEndEffectorSpecification root;
    public AbsoluteEndEffectorSpecification getRoot() { return root; }

    /**
     * Builds a new Relative End effector specification
     * @param root The {@link AbsoluteEndEffectorSpecification} this specification is relative to. If left
     *             {@code null}, then the specification is relative to the arm's absolute specification
     *             at the moment the {@link RelativeEndEffectorSpecification#toAbsolute(CottusArm)} is called
     * @param endEffectorPosition The position difference of the end effector
     * @param endEffectorOrientation The orientation difference of the end effector
     * @param preferredArmAngle The preferred arm angle difference
     */
    private RelativeEndEffectorSpecification(
            AbsoluteEndEffectorSpecification root,
            Vector3D endEffectorPosition, 
            Rotation endEffectorOrientation,
            double preferredArmAngle
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
            AbsoluteEndEffectorSpecification rootSpecification,
            Vector3D endEffectorPosition,
            Rotation endEffectorOrientation,
            double preferredArmAngle
    ) {
        return new RelativeEndEffectorSpecification(rootSpecification, endEffectorPosition, endEffectorOrientation, preferredArmAngle);
    }

    /**
     * Builds a new Relative End effector specification. The specification is relative to the arm's absolute specification
     * at the moment the {@link RelativeEndEffectorSpecification#toAbsolute(CottusArm)} is called
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
    public List<Double> getAnglesFor(CottusArm cottusArm) {
        return null;
    }

    /**
     * Convert the relative specification to an absolute specification given 
     * the context of the current arm's state
     * @param arm The arm's state
     * @return The absolute specification for the current context
     */
    public AbsoluteEndEffectorSpecification toAbsolute(CottusArm arm) {
        AbsoluteEndEffectorSpecification root = this.root;
        if (root == null) { root = arm.getEndEffectorSpecification(); }
        
        return new AbsoluteEndEffectorSpecification(
                root.getEndEffectorPosition().plus(this.getEndEffectorPosition()),
                root.getEndEffectorOrientation().plus(this.getEndEffectorOrientation()),
                root.getPreferredArmAngle() + this.getPreferredArmAngle()
        );
    }
}
