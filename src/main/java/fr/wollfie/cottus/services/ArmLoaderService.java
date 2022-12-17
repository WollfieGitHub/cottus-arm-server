package fr.wollfie.cottus.services;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.JointBounds;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;

public interface ArmLoaderService {

    /**
     * Build a new arm from the dhTable given as a parameter
     * @param dhTable The Denavit-Hartenberg Parameters Table to build the arm from
     * @param bounds The bounds for each joint, from root to end effector
     * @return The newly created arm
     */
    CottusArm buildNewArmFrom(DHTable dhTable, JointBounds[] bounds);
    
    /** @return True if the arm was already built and needs to be loaded */
    boolean isBuilt();
    
    /** Loads the arm that was already previously built */
    CottusArm load();
}
