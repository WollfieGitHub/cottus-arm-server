package fr.wollfie.cottus.services;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.specification.ArmSpecification;
import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.specification.AngleSpecification;

public interface ArmManipulatorService {
    
    /**
     * Move the arm using the angles found in the given specifications.
     * @param specification A specification of all dofs of the arm
     * @throws AngleOutOfBoundsException if one of the specification is not in bounds with the 
     * maximum/minimum angles of the robot
     */
    void moveGiven(AngleSpecification specification) throws AngleOutOfBoundsException;

    /**
     * Move the driven arm using the angles found in the given specifications.
     * @param specification A specification of all dofs of the arm
     */
    void moveDrivenGiven(AngleSpecification specification);
    
    /** @return The current state of the arm, i.e., a pointer to the Arm Object */
    CottusArm getArmState();

    /** @return The current state of the driven arm */
    CottusArm getDrivenArmState();

    /** Sets the given joint the specified angle */
    void setAngle(int jointIndex, double angleRad) throws AngleOutOfBoundsException;

    /**
     * Return the angle of the joint specified by the index
     * @param jointIndex The index of the joint
     * @return The angle of the joint
     */
    double getAngle(int jointIndex);
    
    /** Sets the arm ready for receiving commands */
    void setReady();

    /** Moves the arm given a specification for it */
    void moveGiven(ArmSpecification specification) throws AngleOutOfBoundsException, NoSolutionException;
}
