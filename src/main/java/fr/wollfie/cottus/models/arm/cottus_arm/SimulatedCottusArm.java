package fr.wollfie.cottus.models.arm.cottus_arm;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.dto.JointBounds;
import fr.wollfie.cottus.models.arm.positioning.joints.JointImpl;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.models.arm.positioning.transform.DHBasedJointTransform;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The simulated cottus arm, its movement speed is infinite and
 * its position is controlled live by the user.
 * The record type already redefines the interface's methods for us
 */
public final class SimulatedCottusArm implements CottusArm {
    
    private final List<Joint> joints = new ArrayList<>();
    private final DHTable dhTable;
    private boolean ready;
    
    public SimulatedCottusArm(
            DHTable dhTable, List<JointBounds> bounds
    ) {
        for (int i = 0; i < dhTable.size(); i++) {
            // Create the new articulation
            joints.add(new JointImpl(
                    dhTable.getName(i),
                    i == 0 ? null : joints.get(i - 1), bounds.get(i),
                    new DHBasedJointTransform(dhTable, i),
                    dhTable.isVirtual(i)
            ));
        }
        this.dhTable = dhTable;
    }
    
    @Override public List<Joint> joints() { return joints; }
    @Override public DHTable dhTable() { return dhTable; }
    @Override public boolean isReady() { return ready; }
    @Override public void setReady(boolean ready) { this.ready = ready; }
    
}
