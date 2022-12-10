package fr.wollfie.cottus.models.arm.cottus_arm;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;

import java.util.List;

/**
 * The driven cottus arm, it is wrapped around an existing
 * {@link SimulatedCottusArm} but its movement speed is finite
 * and its position is updated with time. It will send its instructions to
 * the real arm, and as such, is a visualization of the real robotic arm's position
 * in the software
 */
public class DrivenCottusArm implements CottusArm {
    
    @Override
    public List<Joint> getArticulations() {
        return null;
    }

    @Override
    public DHTable getDHTable() {
        return null;
    }
}
