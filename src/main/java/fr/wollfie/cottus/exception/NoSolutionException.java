package fr.wollfie.cottus.exception;

import fr.wollfie.cottus.dto.specification.ArmSpecification;

/** Used when there is no found solution to a problem. Often means
 * that the operation depending on the resolution of the problem has
 * been aborted and its effect cannot be observed */
public class NoSolutionException extends Exception {

    public NoSolutionException() { }

    public NoSolutionException(String message) { super(message); }
}
