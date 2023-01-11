package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.JointBounds;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.models.arm.positioning.specification.AbsoluteEndEffectorSpecification;
import fr.wollfie.cottus.utils.maths.Axis3D;
import fr.wollfie.cottus.utils.maths.Vector;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.matrices.MatrixUtil;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import org.ejml.simple.SimpleMatrix;

import java.util.List;

public interface IKSolver {
    
    double Delta = 1e-5;

    /**
     * Solve the inverse kinematics problem given the position in cartesian space of the end effector
     * and a way to compute the error between the current position and the desired position
     * @param arm The arm 
     * @param endEffectorSpecification Position, rotation, and preferred arm angle in 3D space
     * @param maxRotError The max acceptable error for the rotation in radians
     * @throws NoSolutionException When there is no acceptable solution for the given parameters
     */
    List<Double> startIKSolve(
            CottusArm arm,
            AbsoluteEndEffectorSpecification endEffectorSpecification,
            double maxPosError, double maxRotError
    ) throws NoSolutionException;
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                               UTIL METHODS TO COMPUTE JACOBIAN                       ||
// ||                                                                                      ||
// \\======================================================================================//
    
    /**
     * Compute the jacobian matrix using the delta of {@code delta} for differentiation and
     * @param n The number of joints
     * @param delta The difference used for approximation of differentiation
     * @param table The DH Table
     * @param xT The current position of the end effector
     * @param qT The current set of angles
     * @return The Jacobian Matrix
     */
    static SimpleMatrix computeJacobianWithDiff(
            int n, double delta,
            DHTable table,
            Vector xT, Vector qT
    ) {
        // https://robotics.stackexchange.com/questions/16759/jacobian-of-a-6dof-arm
        Vector[] cols = new Vector[n];
        Vector diff;
        for (int c = 0; c < n; c++) {
            if (table.isVirtual(c)) { diff = Vector.Zero(n); }
            else { diff = Vector.unit(c, n).scaled(delta); }

            cols[c] = xT.minus(KinematicsModule.forward(table, qT.plus(diff), false)).scaled(1/delta);
        }

        return MatrixUtil.from(cols);
    }

    /**
     * Compute the Jacobian using the cross product approximation
     * @param n The number of joints
     * @param table The DH Table
     * @param xT The current position & rotation of the end effector
     * @return The Jacobian Matrix
     */
    static SimpleMatrix computeJacobianWithCross(int n, DHTable table, Vector xT) {
        // https://robotics.stackexchange.com/questions/16759/jacobian-of-a-6dof-arm
        Vector jI;
        Vector[] cols = new Vector[n];

        Vector3D oI, zI, jI02;
        // Position of the end effector
        Vector3D oN = xT.extract3D();
        for (int i = 0; i < n; i++) {
            SimpleMatrix tIN = table.getTransformMatrix(0, i);
            oI = MatrixUtil.multHt(tIN, Vector3D.Zero);
            // Transform the z Axis (Axis of Rotation) and subtract the origin to get the axis at origin
            zI = MatrixUtil.multHt(tIN, Axis3D.Z.unitVector).minus(oI);

            jI02 = zI.cross(oN.minus(oI));
            jI = new Vector(jI02.x, jI02.y, jI02.z, zI.x, zI.y, zI.z);

            cols[i] = jI;
        }

        return MatrixUtil.from(cols);
    }

    /** Clamp all the angles to the bounds of their joints */
    static Vector getBoundedAngles(Vector initialAngles, List<JointBounds> bounds) {
        double[] clampedValues = new double[initialAngles.dim];
        for (int i = 0; i < initialAngles.dim; i++) { clampedValues[i] = bounds.get(i).clamped(initialAngles.get(i)); }
        return new Vector(clampedValues);
    }

// //======================================================================================\\
// ||                                                                                      ||
// ||                                       ERROR CALCULATION                              ||
// ||                                                                                      ||
// \\======================================================================================//
    
    /** Weight of position */
    double ALPHA = 1.0/1000.0;
    /** Weight of rotation */
    double BETA = 1;
    
    /** @return The error in position of the current end effector configuration */
    static double getPosErrorFrom(Vector xCurr, Vector xGoal) {
        return (xGoal.get(0)-xCurr.get(0)) * (xGoal.get(0)-xCurr.get(0))
                + (xGoal.get(1)-xCurr.get(1)) * (xGoal.get(1)-xCurr.get(1))
                + (xGoal.get(2)-xCurr.get(2)) * (xGoal.get(2)-xCurr.get(2));
    }

    /** @return The error in rotation of the current end effector configuration */
    static double getRotErrorFrom(Vector xCurr, Vector xGoal) {
        return (xGoal.get(3) - xCurr.get(3)) * (xGoal.get(3) - xCurr.get(3))
                + (xGoal.get(4) - xCurr.get(4)) * (xGoal.get(4) - xCurr.get(4))
                + (xGoal.get(5) - xCurr.get(5)) * (xGoal.get(5) - xCurr.get(5));
    }

    /** @return The index of the bred item with minimum fitness */
    static boolean errorIsUnderThreshold(Vector xCurr, Vector xGoal, double maxPosError, double maxRotError) {
        return getPosErrorFrom(xCurr, xGoal) <= maxPosError && getRotErrorFrom(xCurr, xGoal) <= maxRotError;
    }

    /** Weighted error between current and final */
    static double getFitness(Vector xCurr, Vector xFinal) {
        return ALPHA * IKSolver.getPosErrorFrom(xCurr, xFinal)
                + BETA * IKSolver.getRotErrorFrom(xCurr, xFinal);
    }
}
