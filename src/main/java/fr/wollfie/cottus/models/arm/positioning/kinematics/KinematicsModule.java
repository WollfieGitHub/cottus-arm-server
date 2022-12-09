package fr.wollfie.cottus.models.arm.positioning.kinematics;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.matrices.Matrix;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

/**
 * Kinematic Analysis :
 * The specification of each joint of the arm relative to the previous one (Excluding the end effector's)
 * 
 * R1 Orthog R2 Orthog R3 Orthog R4 Orthog Orthog R5 Orthog R6
 */
public class KinematicsModule {
    // Static class, cannot be instantiated
    private KinematicsModule() {}
    
    
    
    private static Matrix getTransformationMatrix(CottusArm arm, int articulationIndex) {
        double alphaPrev = arm.getArticulations().get(articulationIndex-1).getAngleRad();
    }

    /**
     * TODO: 12/8/2022 DOCUMENT THIS FUNCTION 
     * @param arm The state of the arm. Info on actuators max/min angles and length will be used
     * @param endEffectorPosition The position of the end effector in world space
     * @param endEffectorOrientation The orientation of the end effector in world space
     * @return The set of angles that allow the arm to position itself as desired
     * @throws NoSolutionException If there is no solution to set the angles to obtain the desired arm configuration
     * 
     * @implNote From "Solving Kinematics Problems of a 6-DOF Robot Manipulator", Alireza Khatamian: 
     * <p>
     *     A robot manipulator’s forward kinematics problem is
     *     solved by attaching a single frame to each joint along with
     *     the robot’s base. Each frame describes the position and
     *     orientation of each joint of the robot relative to the base or
     *     any other global coordinate. Attaching these frames to the
     *     joints reduces the calculation of the robot’s end effector’s
     *     position and orientation to a coordinate translation problem
     *     which is solved by transformation matrices.
     * </p>
     */
    public static double[] inverseSolve(
            CottusArm arm,
            Vector3D endEffectorPosition,
            Rotation endEffectorOrientation
    ) throws NoSolutionException {

        
        double q1, q2, q3, q4, q5, q6;
        double alpha, beta, gamma;
        
        
        return new double[0];
    }
}
