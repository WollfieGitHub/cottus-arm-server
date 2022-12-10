package fr.wollfie.cottus.models.arm.cottus_arm;

import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.models.arm.positioning.articulations.JointImpl;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.services.ArmBuilderService;
import fr.wollfie.cottus.utils.maths.matrices.Matrix;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ArmBuilderImpl implements ArmBuilderService {


    @Override
    public CottusArm buildArmFrom(DHTable dhTable) {
        List<Joint> joints = new ArrayList<>();
        for (int i = 0; i < dhTable.size(); i++) {
            // The matrix to transform i-1 to i
            final Matrix parentToChild = dhTable.getTransformMatrix(i);
            
            // Create the new articulation
            joints.add(new JointImpl(
                    String.format("Articulation %d", i),
                    i == 0 ? null : joints.get(i-1),
                    transform -> transform.setTransform(parentToChild)
            ));
        }
        return new SimulatedCottusArm();
    }
}
