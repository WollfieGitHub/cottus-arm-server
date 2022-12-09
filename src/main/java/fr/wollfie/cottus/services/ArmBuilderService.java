package fr.wollfie.cottus.services;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;

public interface ArmBuilderService {

    /**
     * Build a new arm from the dhTable given as a parameter
     * @param dhTable The Denavit-Hartenberg Parameters Table to build the arm from
     * @return The newly created arm
     */
    CottusArm buildArmFrom(DHTable dhTable);
}
