package fr.wollfie.cottus.models.arm.positioning.specification;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.specification.ArmSpecification;
import fr.wollfie.cottus.utils.maths.Vector3D;

import java.util.ArrayList;
import java.util.List;

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
    public List<Double> getAnglesFor(CottusArm cottusArm) {
        List<Double> angles = new ArrayList<>();

        angles.set(6, endEffectorAngleRad);
        return angles;
    }
}
