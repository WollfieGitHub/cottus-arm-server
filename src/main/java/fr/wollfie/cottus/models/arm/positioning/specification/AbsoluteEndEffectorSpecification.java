package fr.wollfie.cottus.models.arm.positioning.specification;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.specification.EndEffectorSpecification;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.KinematicsModule;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class AbsoluteEndEffectorSpecification extends EndEffectorSpecification {
    
    public AbsoluteEndEffectorSpecification(
            @JsonProperty("endEffectorPosition") Vector3D endEffectorPosition,
            @JsonProperty("endEffectorRotation") Rotation endEffectorOrientation,
            @JsonProperty("armAngle") double armAngle
    ) {
        super(endEffectorPosition, endEffectorOrientation, armAngle);
    }

    @Override
    public List<Double> getAnglesFor(CottusArm cottusArm) throws NoSolutionException {
        try {
            return KinematicsModule.inverseSolve(cottusArm, this).get();
        } catch (InterruptedException | ExecutionException e) {
            // If there are no solutions, then the list of angles is empty
            if (e instanceof ExecutionException execE && execE.getCause() instanceof NoSolutionException noSolE) {
                throw noSolE;
                
            // Otherwise the future was interrupted
            } else { throw new RuntimeException(e); }
        }
    }

    @Override
    public String toString() {
        return String.format("AbsoluteEndEffectorSpecification{%s, %s, %5.3f}",
                this.getEndEffectorPosition(),
                this.getEndEffectorOrientation(),
                this.getPreferredArmAngle());
    }
}
