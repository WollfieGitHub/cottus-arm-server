package fr.wollfie.cottus.services;

import fr.wollfie.cottus.dto.specification.ArmSpecification;
import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.exception.AngleOutOfBoundsException;

public interface ArmManipulatorService {
    
    /**
     * Move the arm using the angles found in the given specifications.
     * @param specification A specification of all dofs of the arm
     * @throws AngleOutOfBoundsException if one of the specification is not in bounds with the 
     * maximum/minimum angles of the robot
     */
    void moveGiven(ArmSpecification specification) throws AngleOutOfBoundsException;

    /** @return The current state of the arm, i.e., a pointer to the Arm Object */
    CottusArm getArmState();

    /** Sets the given joint the specified angle */
    void setAngle(int jointIndex, double angleRad) throws AngleOutOfBoundsException;

    /**
     * Return the angle of the joint specified by the index
     * @param jointIndex The index of the joint
     * @return The angle of the joint
     */
    double getAngle(int jointIndex);
}
