package fr.wollfie.cottus.dto;

import java.util.List;

/**
 * A set of parameters that fully specify the state of the arm at a given
 * point in time. It should remove all degrees of freedom
 */
public interface ArmSpecification {

    /**
     * @return The *7* angles, from root to end effector,
     */
    List<Double> getAngles();
}
