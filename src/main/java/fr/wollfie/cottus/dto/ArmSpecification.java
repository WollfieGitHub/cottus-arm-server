package fr.wollfie.cottus.dto;

/**
 * A set of parameters that fully specify the state of the arm at a given
 * point in time. It should remove all degrees of freedom
 */
public interface ArmSpecification {

    /**
     * @return The *7* angles, from root to end effector,
     */
    double[] getAngles();
}
