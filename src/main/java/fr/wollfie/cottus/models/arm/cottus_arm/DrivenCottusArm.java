package fr.wollfie.cottus.models.arm.cottus_arm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.models.arm.positioning.joints.DrivenJoint;
import fr.wollfie.cottus.models.arm.positioning.joints.JointImpl;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.models.arm.positioning.transform.DHBasedJointTransform;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayList;
import java.util.List;

/**
 * The driven cottus arm, it is wrapped around an existing
 * {@link SimulatedCottusArm} but its movement speed is finite
 * and its position is updated with time. It will send its instructions to
 * the real arm, and as such, is a visualization of the real robotic arm's position
 * in the software
 */
public class DrivenCottusArm implements CottusArm {

    private final CottusArm arm;
    private final DHTable dhTable;
    private final List<DrivenJoint> joints = new ArrayList<>();

    public DrivenCottusArm(CottusArm arm) { 
        this.arm = arm;
        this.dhTable = arm.dhTable().copy();
        for (int i = 0; i < dhTable.size(); i++) {
            // Create the new articulation
            joints.add(new DrivenJoint(
                    arm.getJoint(i),
                    i == 0 ? null : joints.get(i - 1), arm.getJoint(i).getBounds(),
                    new DHBasedJointTransform(this.dhTable, i)
            ));
        }
    }

    @Override public boolean isReady() { return this.arm.isReady(); }
    @Override public List<Joint> joints() { return this.joints.stream().map(j -> (Joint)j).toList(); }
    @Override public DHTable dhTable() {
        return this.dhTable;
    }
    
    @Override 
    public void setAngle(int jointIndex, double angleRad) throws AngleOutOfBoundsException {
        
    }

    @Override public void setReady() { throw new UnsupportedOperationException("A Driven Arm cannot be set ready"); }
}
