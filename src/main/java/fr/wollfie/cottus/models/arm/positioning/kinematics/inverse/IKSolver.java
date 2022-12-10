package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse;

import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;

import java.util.Collection;

@FunctionalInterface
public interface IKSolver {

    Collection<IKSolution> solve(DHTable table) throws NoSolutionException;

}
