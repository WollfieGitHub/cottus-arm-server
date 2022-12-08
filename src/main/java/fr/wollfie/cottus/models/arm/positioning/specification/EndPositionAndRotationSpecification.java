package fr.wollfie.cottus.models.arm.positioning.specification;

import fr.wollfie.cottus.dto.ArmSpecification;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

public class EndPositionAndRotationSpecification implements ArmSpecification {

    private final double endEffectorAngleRad;

    public EndPositionAndRotationSpecification(
            Vector3D endEffectorPosition,
            Rotation endEffectorOrientation,
            double endEffectorAngleRad
    ) {
        this.endEffectorAngleRad = endEffectorAngleRad;
        // TODO
    }
    
    @Override
    public double[] getAngles() {
        double[] angles = new double[7];
        
        angles[6] = endEffectorAngleRad;
        return angles;
    }
}
