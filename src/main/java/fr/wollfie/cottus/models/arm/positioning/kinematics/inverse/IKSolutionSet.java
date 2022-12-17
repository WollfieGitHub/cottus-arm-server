package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse;

import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;

import java.util.*;
import java.util.stream.Stream;

public class IKSolutionSet {
    
    
    /** The solutions for IK, solutions[1] is the possible solutions for theta1, etc... */
    private final List<Double> solutions;
    private final Set<IKTag> tags;

    /** Constructor, level must be specified */
    private IKSolutionSet(List<Double> solutions, Set<IKTag> tags) {
        this.solutions = solutions;
        this.tags = tags;
    }
    
    /** Create a new set of solutions, fully empty */
    public static IKSolutionSet createNew() { 
        return new IKSolutionSet(new ArrayList<>(), new HashSet<>());
    }

    /**
     * Create a new set of solutions, from a previously existing set and a new solution
     * @param prev The existing set of solution
     * @param solution A new solution
     * @return A new set of solutions with the new solution
     */
    public static IKSolutionSet from(int thetaIndex, IKSolutionSet prev, IKSolution solution) {
        // Add new solution
        List<Double> solutions = new ArrayList<>(prev.solutions);
        solutions.set(thetaIndex-1, solution.value);
        
        // Add new tag if any
        Set<IKTag> ikTags = new HashSet<>(prev.tags);
        if (solution.tag != null) { ikTags.add(solution.tag); }
        
        return new IKSolutionSet(solutions, ikTags);
    }

    /** @return True if the solution set has all the specified tags, false otherwise */
    public boolean hasAllTags(IKTag... tags) { return this.tags.containsAll(List.of(tags)); }
    
    /**
     * Add all the solutions for the joint and divide into a new IKSolutions object
     * for each of the solution
     * @param solver  The solver which gives new solutions
     * @param dhTable The original dhtable
     */
    public Stream<IKSolutionSet> divide(int thetaIndex, IKSolver solver, DHTable dhTable) {
        Collection<IKSolution> solutions;
        try {
            solutions = solver.solve(pipeIntoTable(dhTable));
            
        } catch (NoSolutionException e) { solutions = Collections.emptyList(); }
            
        return solutions.stream().map(solution -> from(thetaIndex, this, solution));
    }

    /**
     * Pipe the solutions of this solution set into the given table and returns a 
     * copy of the table
     * @param table The table to modify
     * @return A copy of the given table
     */
    public DHTable pipeIntoTable(DHTable table) {
        table = table.copy();
        for (int i = 0; i < this.solutions.size(); i++) { table.setTheta(i, this.solutions.get(i)); }
        
        return table;
    }
    
    /** @return All solutions found so far */
    public List<Double> getAll() {
        return new ArrayList<>(this.solutions);
    }
}
