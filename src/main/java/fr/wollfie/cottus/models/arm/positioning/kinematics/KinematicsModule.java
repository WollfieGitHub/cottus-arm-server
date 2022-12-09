package fr.wollfie.cottus.models.arm.positioning.kinematics;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.utils.maths.Axis3D;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.matrices.HTMatrix;
import fr.wollfie.cottus.utils.maths.matrices.Matrix;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

import static java.lang.Math.*;

/**
 * Kinematic Analysis :
 * The specification of each joint of the arm relative to the previous one (Excluding the end effector's)
 * 
 * R1 Orthog R2 Orthog R3 Orthog R4 Orthog Orthog R5 Orthog R6
 */
public class KinematicsModule {
    // Static class, cannot be instantiated
    private KinematicsModule() {}
    
    /**
     * TODO: 12/8/2022 DOCUMENT THIS FUNCTION 
     * @param arm The state of the arm. Info on actuators max/min angles and length will be used
     * @param endEffectorPosition The position of the end effector in world space
     * @param endEffectorRotation The orientation of the end effector in world space
     * @return The set of angles that allow the arm to position itself as desired
     * @throws NoSolutionException If there is no solution to set the angles to obtain the desired arm configuration
     * 
     * @implNote From "Solving Kinematics Problems of a 6-DOF Robot Manipulator", Alireza Khatamian: 
     * The computation of the inverse kinematics given in these papers were adapted to the robot arm's configuration
     */
    public static double[] inverseSolve(
            CottusArm arm,
            Vector3D endEffectorPosition,
            Rotation endEffectorRotation
    ) throws NoSolutionException {
        Vector3D nRight = Axis3D.X.getUnitVector().rotatedAtOriginUsing(endEffectorRotation.getEulerAngles());
        Vector3D nDir = Axis3D.Z.getUnitVector().rotatedAtOriginUsing(endEffectorRotation.getEulerAngles());
        Vector3D nUp = Axis3D.Y.getUnitVector().rotatedAtOriginUsing(endEffectorRotation.getEulerAngles());
        
        Vector3D P = endEffectorPosition;
        // Let's name <endEffectorPosition> "P"
        // and <endEffectorDirection> "N" for the documentation
        // Let's name P_itoj the position of the jth articulation relative to the ith articulation
        // Let's name Ki the frame of reference of articulation i
        
        DHTable dhTable = arm.getDHTable();
        
        // In rotation matrix, first line is "right", second is "up", third is "forward"
        // The transform from the base to the desired position and direction
        Matrix T0toG = new Matrix(new double[][]{
                { nRight.x, nRight.y, nRight.x, P.x},
                {    nUp.x,    nUp.y,    nUp.y, P.y},
                {   nDir.x,   nDir.y,   nDir.z, P.z},
                {        0,      0,          0,   1}});
        
//=========   ====  == =
//      COMPUTE THETA 1
//=========   ====  == =
        
        // The 6th joint's space is aligned with N such that P_5to6 = d6 * N
        // And P_0to6 = [T0to6_03, T0to6_13, T0to6_23] (Offset in the transformation)
        // From which we have P_0to5 = P_0to6 - P_5to6 which yields
        double theta1_1 = atan2(
                T0toG.get(1,3) - dhTable.getD(6)*T0toG.get(1,2),
                T0toG.get(0,3) - dhTable.getD(6)*T0toG.get(0,2));
        double theta1_2 = theta1_1+PI;
        
//=========   ====  == =
//      COMPUTE THETA 3
//=========   ====  == =
        
        // Second, we want to compute the distance between  P_2to5
        // for which we need P_0to2 and setting theta2 to 0, we can get theta3
        Vector3D P_0to5 = dhTable.getTransformMatrix(0, 4).getPosition();
        Vector3D P_0to2 = dhTable.getTransformMatrix(0, 2).getPosition();
        
        double l1 = 1, a2 = dhTable.getA(2);
        Vector3D P_2to5 = P_0to5.subtract(P_0to2);
        double P_2to5_norm = P_2to5.norm();
        double P_2to5_norm_2 = P_2to5.normSquared();
        
        double temp0 = ( l1*l1 - a2*a2 + P_2to5_norm_2)
                        / (2*P_2to5_norm);
        double phi = asin(temp0/l1) + asin((P_2to5_norm - temp0) / a2);
        double alpha = atan2(-dhTable.getD(4), dhTable.getA(3));
        
        double theta3_1 = PI - phi - alpha;
        double theta3_2 = PI + phi - alpha;

        
//=========   ====  == =
//      COMPUTE THETA 2
//=========   ====  == =
        
        return new double[0];
    }
}
