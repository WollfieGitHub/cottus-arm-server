package fr.wollfie.cottus.models.arm.positioning.specification;

import fr.wollfie.cottus.dto.ArmSpecification;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.InverseKinematicModule;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

import java.util.ArrayList;
import java.util.List;

public abstract class EndEffectorSpecification implements ArmSpecification {

    private final double endEffectorAngleRad;
    private final Vector3D endEffectorPosition;
    private final Rotation endEffectorOrientation;

    public EndEffectorSpecification(
            Vector3D endEffectorPosition,
            Rotation endEffectorOrientation,
            double endEffectorAngleRad
    ) {
        this.endEffectorAngleRad = endEffectorAngleRad;
        this.endEffectorPosition = endEffectorPosition;
        this.endEffectorOrientation = endEffectorOrientation; 
    }
}
