package fr.wollfie.cottus.models.animation;

import fr.wollfie.cottus.dto.animation.ArmAnimation;
import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.services.ArmStateService;
import fr.wollfie.cottus.services.arm_controller.ArmAnimatorControllerService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Handles the animation of the arm when it is active
 */
@ApplicationScoped
public class AnimationController implements ArmAnimatorControllerService {
    
    @Inject
    ArmStateService armStateService;
    
    private boolean active = false;
    @Override public void setActive(boolean active) { this.active = active; }
    
    /** Current animation playing, or null if no animation is playing */
    private ArmAnimation current;
    
    private long timeStarted = System.currentTimeMillis();
    
    @Override
    public void update() {
        if (!this.active) { this.clearAnimation(); }
        if (this.current == null) { return; }

        double dt = (System.currentTimeMillis() - timeStarted) / 1000.0;

        // Animation is over
        if (this.current.getDurationSecs() <  dt) { this.clearAnimation(); return; }

        try {
            armStateService.moveGiven(this.current.evaluateAt(dt));
        } catch (AngleOutOfBoundsException | NoSolutionException e) { /* Drop frame and continue */ }
    }

    @Override
    public boolean isPlayingAnimation() { return current != null; }

    @Override
    public boolean playAnimation(ArmAnimation animation) {
        if (this.current != null) { return false; }
        
        this.armStateService.setReady(false);
        this.active = true;
        this.current = animation;
        this.timeStarted = System.currentTimeMillis();
        return true;
    }
    
    /** Stops the animation from playing and reset the arm's state back to default */
    private void clearAnimation() {
        this.current = null;
        this.active = false;
        this.armStateService.setReady(true);
    }
    
}
