package fr.wollfie.cottus.models.arm.positioning.specification;

import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

public class AbsoluteEndEffectorSpecification extends EndEffectorSpecification{
    
    public AbsoluteEndEffectorSpecification(Vector3D endEffectorPosition, Rotation endEffectorOrientation, double endEffectorAngleRad) {
        super(endEffectorPosition, endEffectorOrientation, endEffectorAngleRad);
    }
}
