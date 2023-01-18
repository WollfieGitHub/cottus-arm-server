package fr.wollfie.cottus.exception;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.specification.ArmSpecification;
import fr.wollfie.cottus.dto.specification.EndEffectorSpecification;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.KinematicsModule;

/** 
 * Used when there is no found solution to a problem. Often means
 * that the operation depending on the resolution of the problem has
 * been aborted and its effect cannot be observed. 
 * Used by {@link KinematicsModule#inverseSolve(CottusArm, EndEffectorSpecification)}
 */
public class NoSolutionException extends Exception {

    public NoSolutionException() { }

    public NoSolutionException(String message) { super(message); }
}
