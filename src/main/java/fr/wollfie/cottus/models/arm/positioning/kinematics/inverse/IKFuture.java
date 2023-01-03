package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse;

import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.utils.maths.Vector3D;

import java.util.List;
import java.util.function.Consumer;

public class IKFuture {

    private IKCallback onSolutionUpdate;
    private Consumer<NoSolutionException> eHandler;
    
    private IKFuture() {}
    
    /** @return A new IKFuture object */
    public static IKFuture createNew() { return new IKFuture(); }

    /**
     * Sets the callback that gets invoked when the solver updates a solution
     * @param onSolutionUpdate What to do when a new solution is updated by the solver
     * @return This {@link IKFuture} object
     */
    public IKFuture onSolution(IKCallback onSolutionUpdate) { 
        this.onSolutionUpdate = onSolutionUpdate;
        return this;
    }

    /**
     * Sets the callback that gets invoked when the solver throws a {@link NoSolutionException}
     * @param eHandler A function that handles the exception
     * @return This {@link IKFuture} object
     */
    public IKFuture onFailure(Consumer<NoSolutionException> eHandler) {
        this.eHandler = eHandler;
        return this;
    }

    /**
     * Updates the {@link IKFuture} object with new solution angles
     * @param angles The solution angles
     */
    public void update(List<Double> angles) {
        if (this.onSolutionUpdate == null) { return; }
        
        try {
            this.onSolutionUpdate.onValue(angles);
        } catch (AngleOutOfBoundsException e) { this.fail(); }
    }
    
    public void update(AnalyticalIKSolutionProcessor solutionProcessor) {
        this.update(solutionProcessor.process());
    }
    
    /**
     * Throw a new {@link NoSolutionException} for this future
     */
    public void fail() {
        if (this.eHandler != null) { this.eHandler.accept(new NoSolutionException()); }
        else { throw new RuntimeException("No exception handler configured for this future"); }
    }
    
}
