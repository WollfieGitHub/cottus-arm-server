package fr.wollfie.cottus.models.animation;

import fr.wollfie.cottus.dto.ArmAnimation;
import fr.wollfie.cottus.services.arm_controller.ArmAnimatorControllerService;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AnimationController implements ArmAnimatorControllerService {
    
    private boolean active = false;
    @Override public void setActive(boolean active) { this.active = active; }
    
    /** Current animation playing, or null if no animation is playing */
    private ArmAnimation current;
    
    @Override
    public void update() {
        if (!this.active) { 
            if (this.current != null) { this.current = null; }
            return;
        }
        
    }

    @Override
    public boolean isPlayingAnimation() { return current != null; }
}
