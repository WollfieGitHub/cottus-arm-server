package fr.wollfie.cottus.models.arm.positioning.specification;

import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

public class RelativeEndEffectorSpecification extends EndEffectorSpecification{

    /** True if this specification is the root for others, meaning it should be considered,
     *  once its position is evaluated, as 0 for all following {@link RelativeEndEffectorSpecification}
     *  that are not root*/
    private boolean root = false;
    public boolean isRoot() { return this.root; }
    public void setRoot(boolean root) { this.root = root; }

    public RelativeEndEffectorSpecification(Vector3D endEffectorPosition, Rotation endEffectorOrientation, double endEffectorAngleRad) {
        super(endEffectorPosition, endEffectorOrientation, endEffectorAngleRad);
    }
}
