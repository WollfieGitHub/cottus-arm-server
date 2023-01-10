package fr.wollfie.cottus.services.arm_controller;

import fr.wollfie.cottus.dto.specification.EndEffectorSpecification;
import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.utils.maths.Axis3D;
import fr.wollfie.cottus.utils.maths.Vector3D;

public interface ArmManualControllerService extends ArmControllerService {
    
    /**
     * Position the end effector with the desired position and rotation, specified as euler angles,
     * @param endEffectorSpecification A specification for position, rotation in 3D space and preferred arm angle
     */
    void moveTo(EndEffectorSpecification endEffectorSpecification);

    /**
     * Rotate the joint to the specified angle in radians
     * @param jointIndex The index of the joint to move
     * @param angleRad The angle in radians
     */
    void setAngle(int jointIndex, double angleRad) throws AngleOutOfBoundsException;
    
    /**
     * Return the angle of the joint specified by the index
     * @param jointIndex The index of the joint
     * @return The angle of the joint
     */
    double getAngle(int jointIndex);

}
