package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse;

import fr.wollfie.cottus.exception.AngleOutOfBoundsException;

import java.util.List;

@FunctionalInterface
public interface IKCallback {

    /**
     * Called by the IK Solver each time a new set of angles is 
     * computed
     * @param angles The angles of the arm
     */
    void onValue(List<Double> angles) throws AngleOutOfBoundsException;
}
