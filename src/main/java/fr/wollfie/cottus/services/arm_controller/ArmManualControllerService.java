package fr.wollfie.cottus.services.arm_controller;

import fr.wollfie.cottus.dto.ArmSpecification;
import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.utils.maths.Axis3D;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

public interface ArmManualControllerService extends ArmControllerService {
    
    /**
     * Move the end effector to the specified position, while keeping the elbow at the same place if possible.
     * @param position The position in global space for the end effector
     * @throws NoSolutionException If there is no solution to keep the elbow effector at its position and 
     * position the end effector as desired. 
     */
    void moveEndEffectorTo(Vector3D position) throws NoSolutionException;

    /**
     * Move the elbow effector to the specified position, while keeping the end effector at the same place if possible.
     * @param position The position in global space for the elbow
     * @throws NoSolutionException If there is no solution to keep the end effector at its position and 
     * position the elbow effector as desired. 
     */
    void moveElbowEffectorTo(Vector3D position) throws NoSolutionException;

    /**
     * Position the elbow effector and end effector at the specified positions in global space if possible.
     * @param elbowPosition The position of the elbow effector in global space
     * @param endPosition The position of the end effector in global space
     * @param elbowUp If the elbow should be up (true) or down (false) 
     * @throws NoSolutionException If there is no solution to place the elbow and end effector as desired
     */
    void moveArmWith(Vector3D elbowPosition, Vector3D endPosition, boolean elbowUp) throws NoSolutionException;

    /**
     * Position the end effector with the desired position and rotation, specified as euler angles,
     * @param position The position in global space of the end effector
     * @param rotation The rotation in global space of the end effector
     * @param effectorAngle The rotation angle of the end effector
     * @throws NoSolutionException If there is no solution to place the end effector as desired
     */
    void moveEndEffectorWith(Vector3D position, Rotation rotation, double effectorAngle) throws NoSolutionException;
    
    /** Sets the given joint the specified angle */
    void setAngle(int jointIndex, double angleRad) throws AngleOutOfBoundsException;

    /**
     * Moves the end effector by the specified distance in Millimeters, along the given axis
     * @param amountMm The distance in Mm
     * @param axis The global axis 
     */
    void moveEndEffectorBy(float amountMm, Axis3D axis) throws NoSolutionException;
}
