package fr.wollfie.cottus.models.arm.cottus_arm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.utils.Preconditions;

import java.util.List;

/**
 * The simulated cottus arm, its movement speed is infinite and
 * its position is controlled live by the user.
 */
public record SimulatedCottusArm(
        List<Joint> joints, DHTable dhTable
) implements CottusArm {

    @Override @JsonIgnore
    public DHTable getDHTable() { return dhTable; }

    @Override
    public void setAngles(List<Double> anglesRad) throws AngleOutOfBoundsException {
        Preconditions.checkArgument(anglesRad.size() == this.getNbOfNonVirtualJoints());
        int i = 0;
        for (Joint j : joints) { if (!j.isVirtual()) { j.setAngleRad(anglesRad.get(i)); i++; } }
    }

    @Override
    public void setAngle(int jointIndex, double angleRad) throws AngleOutOfBoundsException {
        joints.get(jointIndex).setAngleRad(angleRad);
    }
}
