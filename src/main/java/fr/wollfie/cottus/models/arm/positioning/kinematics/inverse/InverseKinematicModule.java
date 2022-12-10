package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.utils.Utils;
import fr.wollfie.cottus.utils.maths.Axis3D;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.matrices.Matrix;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

import java.util.List;

import static java.lang.Math.*;

/**
 * Kinematic Analysis :
 * The specification of each joint of the arm relative to the previous one (Excluding the end effector's)
 * 
 * R1 Orthog R2 Orthog R3 Orthog R4 Orthog Orthog R5 Orthog R6
 */
public class InverseKinematicModule {

    private double c1, c2, c3, c4, c5;
    private double s1, s2, s3, s4, s5;
    private double v114, v124;
    private double c23, s23;
    private Vector3D p, w, u, v;
    private double v313, v323, v113;

    // Static class, cannot be instantiated
    private InverseKinematicModule() {}

// //======================================================================================\\
// ||                                                                                      ||
// ||                                       INVERSE KINEMATICS                             ||
// ||                                                                                      ||
// \\======================================================================================//
    
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
    public static List<Double> inverseSolve(
            CottusArm arm,
            Vector3D endEffectorPosition,
            Rotation endEffectorRotation
    ) throws NoSolutionException {
        InverseKinematicModule module = new InverseKinematicModule();
        return module.ikSolve(arm, endEffectorPosition, endEffectorRotation);
    }
    
    private List<Double> ikSolve(
            CottusArm arm,
            Vector3D endEffectorPosition,
            Rotation endEffectorRotation
    ) throws NoSolutionException {
        Vector3D nRight = Axis3D.X.getUnitVector().rotatedAtOriginUsing(endEffectorRotation.getEulerAngles());
        Vector3D nDir = Axis3D.Z.getUnitVector().rotatedAtOriginUsing(endEffectorRotation.getEulerAngles());
        Vector3D nUp = Axis3D.Y.getUnitVector().rotatedAtOriginUsing(endEffectorRotation.getEulerAngles());
        
        // Copy the DH Table so that the arm's isn't actually modified
        DHTable dhTable = arm.getDHTable().copy();
        // Just a security in case it was modified somewhere for no reason
        for (int i = 0; i < dhTable.size(); i++) { dhTable.setTheta(i, 0); }

        // In rotation matrix, first line is "right", second is "up", third is "forward"
        // The transform from the base to the desired position and direction
        Matrix t0toG = new Matrix(new double[][]{
                {nRight.x, nRight.y, nRight.x, endEffectorPosition.x},
                {nUp.x, nUp.y, nUp.y, endEffectorPosition.y},
                {nDir.x, nDir.y, nDir.z, endEffectorPosition.z},
                {0, 0, 0, 1}});

        u = Vector3D.of( t0toG.get(0, 0), t0toG.get(0, 1), t0toG.get(0, 2));
        v = Vector3D.of( t0toG.get(1, 0), t0toG.get(1, 1), t0toG.get(1, 2));
        w = Vector3D.of( t0toG.get(2, 0), t0toG.get(2, 1), t0toG.get(2, 2));

        p = Vector3D.of(
                t0toG.get(0, 3) - dhTable.getD(6)* w.x,
                t0toG.get(1, 3) - dhTable.getD(6)* w.y,
                t0toG.get(2, 3) - dhTable.getD(6)* w.z);
        
        IKSolutionSet solutionSet = IKSolutionSet.createNew().divide(1, solveTheta1(), dhTable)
                .flatMap(s -> s.divide(2, solveTheta2(), dhTable)).parallel()
                .flatMap(s -> s.divide(3, solveTheta3(), dhTable))
                .flatMap(s -> s.divide(4, solveTheta4(), dhTable))
                .flatMap(s -> s.divide(5, solveTheta5(), dhTable))
                .flatMap(s -> s.divide(6, solveTheta6(), dhTable))
                .filter(IKSolutionSet::hasAllTags)
                .findFirst().orElse(null);
        if (solutionSet == null) { throw new NoSolutionException(); }
        List<Double> solutions = solutionSet.getAll();
        if (solutions.isEmpty()) { throw new NoSolutionException(); }
        
        return solutions;
    }

    /**
     * Solve the inverse kinematics for theta1 and return the list of possible values of theta1
     * @return A list of possible solutions
     */
    private IKSolver solveTheta1() {
        return (t) -> {
            // The 6th joint's space is aligned with N such that P_5to6 = d6 * N
            // And P_0to6 = [T0to6_03, T0to6_13, T0to6_23] (Offset in the transformation)
            // From which we have P_0to5 = P_0to6 - P_5to6 which yields
            double r = sqrt(p.x*p.x + p.y*p.y);
            double d2 = t.getD(2);
            // If the radius is shorter that the first link, then we cannot reach it
            if (r < d2) { throw new NoSolutionException(); }

            double k1 = atan2(p.x, p.y);
            double k2 = asin(d2/r);

            return List.of(
                    IKSolution.ofTagged(k1+k2, IKTag.SHOULDER_RIGHT),
                    IKSolution.of(k1-k2+PI)
            );
        };
    }

    /**
     * Solve the inverse kinematics for theta1 and return the list of possible values of theta2
     * @return A list of possible solutions
     */
    private IKSolver solveTheta2() {
        return (t) -> {
            double theta1 = t.getTheta(1);
            c1 = cos(theta1);
            s1 = sin(theta1);

            v114 = p.x* c1 + p.y* s1 - t.getA(1);
            v124 = p.z - t.getD(1);

            double r = sqrt(v114 * v114 + v124 * v124);

            double a2 = t.getA(2), d4 = t.getD(4), a3 = t.getA(3);
            double k1= (a2*a2 - d4*d4 - a3*a3 + v114 * v114 + v124 * v124) / (2*a2*r);
            if (abs(k1) > 1) { throw new NoSolutionException(); }
            
            double k2 = acos(k1);
            return List.of(
                    IKSolution.ofTagged(atan2(v124, v114) + k2, IKTag.ELBOW_UP),
                    IKSolution.of(atan2(v124, v114) - k2)
            );
        };
    }

    /**
     * Solve the inverse kinematics for theta1 and return the list of possible values of theta3
     * @return A list of possible solutions
     */
    private IKSolver solveTheta3() {
        return (t) -> {
            double theta2 = t.getTheta(2);
            c2 = cos(theta2);
            s2 = sin(theta2);

            double v214 =  c2 *v114 + s2 *v124 - t.getA(2);
            double v224 = -s2 *v114 + c2 *v124;

            // TODO We might miss a solution here
            return List.of( IKSolution.of(-atan2(t.getA(3), t.getD(4)) + atan2(v214, -v224)) );
        };
    }

    /**
     * Solve the inverse kinematics for theta1 and return the list of possible values of theta4
     * @return A list of possible solutions
     */
    private IKSolver solveTheta4() {
        return (t) -> {
            c23 = cos(t.getTheta(2)+t.getTheta(3));
            s23 = sin(t.getTheta(2)+t.getTheta(3));
            
            return List.of(
                    IKSolution.ofTagged(getTh4(1), IKTag.WRIST_NOT_FLIPPED), 
                    IKSolution.of(getTh4(-1))
            );
        };
    }
    
    /** Help get both values of theta4 */
    private double getTh4(int n4) {
        v113 = c1*w.x + s1*w.y;
        v313 = c23* v113 + s23*w.z;
        v323 = s1*w.x - c1*w.y;

        double th4;
        double th4Old = 0.0;
        if (Utils.isZero(v323) && Utils.isZero(v313)) { th4 = 0.0; }
        else { th4 = atan2(n4* v323, n4* v313); }

        if (Utils.isZero(v323) && v313 <= 0) { th4 = th4Old; }
        if (v323 >= 0 && v313 <= 0) { th4 = th4 - 2*PI; }
        if (Utils.isZero(v113) && Utils.isZero(v313) && Utils.isZero(v323)) { th4 = th4Old; }
        return th4;
    }

    /**
     * Solve the inverse kinematics for theta1 and return the list of possible values of theta5
     * @return A list of possible solutions
     */
    private IKSolver solveTheta5() {
        return (t) -> {
            c4 = cos(t.getTheta(4));
            s4 = sin(t.getTheta(4));
            
            double k1 = c4*v313 + s4*v323;
            double k2 = s23*v113 - c23*w.z;
            return List.of( IKSolution.of(atan2(k1, k2)) );
        };
    }

    /**
     * Solve the inverse kinematics for theta1 and return the list of possible values of theta6
     * @return A list of possible solutions
     */
    private IKSolver solveTheta6() {
        return (t) -> {
            c5 = cos(t.getTheta(5));
            s5 = sin(t.getTheta(5));

            double v111 = c1*u.x + s1*u.y;
            double v131 = s1*u.x - c1*u.y;
            double v311 = c23*v111 + s23*u.z;
            double v331 = s23*v111 - c23*u.z;
            double v411 = c4*v311 + s4*v131;
            double v431 = -s4*v311 + c4*v131;

            double k1 = v431;
            double k2 = c5*v411 - s5*v331;
            return List.of( IKSolution.of(atan2(k1, k2)) );
        };
    }
}
