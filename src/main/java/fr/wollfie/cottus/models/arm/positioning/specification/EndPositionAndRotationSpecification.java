package fr.wollfie.cottus.models.arm.positioning.specification;

import fr.wollfie.cottus.dto.ArmSpecification;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

import java.util.ArrayList;
import java.util.List;

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
    public List<Double> getAngles() {
        List<Double> angles = new ArrayList<>();
        
        angles.set(6, endEffectorAngleRad);
        return angles;
    }
}
