package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse;

import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.exception.NoSolutionException;

import java.util.List;
import java.util.function.Consumer;

public class IKFuture {

    private IKCallback onUpdate;
    private IKCallback onSolutionComplete;
    private Consumer<NoSolutionException> eHandler;
    
    private IKFuture() {}
    
    /** @return A new IKFuture object */
    public static IKFuture createNew() { return new IKFuture(); }

    /**
     * Sets the callback that gets invoked when the solver updates a solution
     * @param onUpdate What to do when a new partial solution is submitted by the solver
     * @return This {@link IKFuture} object
     */
    public IKFuture onUpdate(IKCallback onUpdate) { 
        this.onUpdate = onUpdate;
        return this;
    }

    /**
     * Sets the callback that gets invoked when the solver updates a solution
     * @param onSolutionFound What to do when a valid solution is found by the solver
     * @return This {@link IKFuture} object
     */
    public IKFuture onComplete(IKCallback onSolutionFound) {
        this.onSolutionComplete = onSolutionFound;
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
    public void updateWith(List<Double> angles) {
        if (this.onUpdate == null) { return; }
        
        try {
            this.onUpdate.onValue(angles);
        } catch (AngleOutOfBoundsException e) { this.fail(); }
    }

    /**
     * Complete the {@link IKFuture} object with new solution angles that are valid
     * @param angles The solution angles
     */
    public void completeWith(List<Double> angles) {
        if (this.onSolutionComplete == null && this.onUpdate != null) { this.updateWith(angles); }
        else if (this.onSolutionComplete != null) {
            try {
                this.onSolutionComplete.onValue(angles);
            } catch (AngleOutOfBoundsException e) { this.fail(); }
        }
    }
    
    public void updateWith(AnalyticalIKSolutionProcessor solutionProcessor) {
        this.updateWith(solutionProcessor.process());
    }
    
    /**
     * Throw a new {@link NoSolutionException} for this future
     */
    public void fail() {
        if (this.eHandler != null) { this.eHandler.accept(new NoSolutionException()); }
        else { throw new RuntimeException("No exception handler configured for this future"); }
    }
    
}
