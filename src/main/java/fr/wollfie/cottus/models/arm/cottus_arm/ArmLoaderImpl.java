package fr.wollfie.cottus.models.arm.cottus_arm;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.dto.JointBounds;
import fr.wollfie.cottus.models.arm.positioning.joints.JointImpl;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.models.arm.positioning.transform.DHBasedJointTransform;
import fr.wollfie.cottus.services.ArmLoaderService;
import io.quarkus.logging.Log;
import org.jboss.resteasy.reactive.common.NotImplementedYet;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class ArmLoaderImpl implements ArmLoaderService {

    @Override
    public CottusArm buildNewArmFrom(DHTable dhTable, JointBounds[] bounds) {
        List<Joint> joints = new ArrayList<>();
        for (int i = 0; i < dhTable.size(); i++) {
            // Create the new articulation
            joints.add(new JointImpl(
                    dhTable.getName(i),
                    i == 0 ? null : joints.get(i - 1), bounds[i],
                    new DHBasedJointTransform(dhTable, i),
                    dhTable.isVirtual(i)
            ));
        }
        return new SimulatedCottusArm(joints, dhTable);
    }

    @Override
    public boolean isBuilt() {
        return false; /* TODO */
    }

    @Override
    public CottusArm load() {
        throw new NotImplementedYet(); /* TODO */
    }
}
