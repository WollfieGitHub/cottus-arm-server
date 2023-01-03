package fr.wollfie.cottus.models.arm.cottus_arm;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;

import java.util.List;

/**
 * The simulated cottus arm, its movement speed is infinite and
 * its position is controlled live by the user.
 * The record type already redefines the interface's methods for us
 */
public record SimulatedCottusArm(
        List<Joint> joints, DHTable dhTable
) implements CottusArm { }
