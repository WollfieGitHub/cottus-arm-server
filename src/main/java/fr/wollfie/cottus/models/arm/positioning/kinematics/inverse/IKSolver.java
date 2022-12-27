package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.utils.maths.Vector;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

import java.util.List;
import java.util.function.BiFunction;

public interface IKSolver {
    
    double Delta = 100.0;

    /**
     * Solve the inverse kinematics problem given the position in cartesian space of the end effector
     * and a way to compute the error between the current position and the desired position
     * @param arm The arm 
     * @param position The position in 3d space
     * @param rotation The rotation in 3d space
     * @param maxError The max acceptable error 
     * @param computeError A way to compute the error
     * @return The list of joint angles to reach the desired position
     */
    List<Double> ikSolve(
            CottusArm arm,
            Vector3D position, Rotation rotation,
            double maxError, BiFunction<Vector, Vector, Double> computeError
    );
}
