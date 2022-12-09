package fr.wollfie.cottus.models.arm.positioning;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.utils.maths.Matrix;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

import static java.lang.Math.*;

public class InverseKinematics {
    // Static class, cannot be instantiated
    private InverseKinematics() {}

    /**
     * TODO: 12/8/2022 DOCUMENT THIS FUNCTION 
     * @param arm The state of the arm. Info on actuators max/min angles and length will be used
     * @param endEffectorPosition The position of the end effector in world space
     * @param endEffectorOrientation The orientation of the end effector in world space
     * @return The set of angles that allow the arm to position itself as desired
     * @throws NoSolutionException If there is no solution to set the angles to obtain the desired arm configuration
     */
    public static double[] solve(
            CottusArm arm,
            Vector3D endEffectorPosition,
            Rotation endEffectorOrientation
    ) throws NoSolutionException {

        double q1, q2, q3, q4, q5, q6;
        double alpha, beta, gamma;
        
        // Rotation of joint 3 wrt to the base frame interms the first three angles q1, q2, q3
        Matrix r03 = new Matrix(
                sin(q2 + q3)*cos(q1), cos(q1)*cos(q2 + q3), -sin(q1),
                sin(q1)*sin(q2 + q3), sin(q1)*cos(q2 + q3),  cos(q1),
                cos(q2 + q3),            -sin(q2 + q3),        0
        );
        
        // Transpose of R03
        Matrix r03Transposed = new Matrix(
                sin(q2 + q3)*cos(q1), sin(q1)*sin(q2 + q3),  cos(q2 + q3),
                cos(q1)*cos(q2 + q3), sin(q1)*cos(q2 + q3), -sin(q2 + q3),
                            -sin(q1),              cos(q1),             0
        );
        
        // Rotation of joint 6 wrt to frame of joint 3 interms of the last three angles q4, q5, q6
        Matrix r36 = new Matrix(
                -sin(q4)*sin(q6) + cos(q4)*cos(q5)*cos(q6), -sin(q4)*cos(q6) - sin(q6)*cos(q4)*cos(q5), -sin(q5)*cos(q4),
                                           sin(q5)*cos(q6),                           -sin(q5)*sin(q6),          cos(q5),
                -sin(q4)*cos(q5)*cos(q6) - sin(q6)*cos(q4),  sin(q4)*sin(q6)*cos(q5) - cos(q4)*cos(q6),  sin(q4)*sin(q5)
        );
        
        // Rotation of urdf_gripper with respect to the base frame interms of alpha = yaw, beta = pitch, gamma = roll
        Matrix r0u = new Matrix(
            cos(alpha)*cos(beta), -sin(alpha)*cos(gamma) + sin(beta)*sin(gamma)*cos(alpha), sin(alpha)*sin(gamma) + sin(beta)*cos(alpha)*cos(gamma),
            sin(alpha)*cos(beta),  sin(alpha)*sin(beta)*sin(gamma) + cos(alpha)*cos(gamma), sin(alpha)*sin(beta)*cos(gamma) - sin(gamma)*cos(alpha),
                      -sin(beta),                                     sin(gamma)*cos(beta),                                    cos(beta)*cos(gamma)
        );
        
        Matrix t0GB = new Matrix(new double[][] {
                {1.0*sin(alpha)*sin(gamma) + sin(beta)*cos(alpha)*cos(gamma),  1.0*sin(alpha)*cos(gamma) - 1.0*sin(beta)*sin(gamma)*cos(alpha), 1.0*cos(alpha)*cos(beta), px},
                {sin(alpha)*sin(beta)*cos(gamma) - 1.0*sin(gamma)*cos(alpha), -1.0*sin(alpha)*sin(beta)*sin(gamma) - 1.0*cos(alpha)*cos(gamma), 1.0*sin(alpha)*cos(beta), py},
                {                                   1.0*cos(beta)*cos(gamma),                                        -1.0*sin(gamma)*cos(beta),           -1.0*sin(beta), pz},
                {                                                          0,                                                                0,                        0,  1}
        });
        
        return new double[0];
    }
}
