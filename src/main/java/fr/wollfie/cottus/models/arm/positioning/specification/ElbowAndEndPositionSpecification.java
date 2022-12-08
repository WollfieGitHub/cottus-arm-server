package fr.wollfie.cottus.models.arm.positioning.specification;

import fr.wollfie.cottus.dto.ArmSpecification;
import fr.wollfie.cottus.utils.maths.Vector3D;

public class ElbowAndEndPositionSpecification implements ArmSpecification {

    private final double endEffectorAngleRad;

    public ElbowAndEndPositionSpecification(
            Vector3D elbowPosition,
            Vector3D endPosition,
            double endEffectorAngleRad
    ) {
        // TODO
        this.endEffectorAngleRad = endEffectorAngleRad;
    }

    @Override
    public double[] getAngles() {
        double[] angles = new double[7];

        angles[6] = endEffectorAngleRad;
        return angles;
    }
}
