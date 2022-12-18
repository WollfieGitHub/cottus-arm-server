package fr.wollfie.cottus.models.animation;

import fr.wollfie.cottus.dto.ArmAnimation;
import fr.wollfie.cottus.services.ArmAnimatorService;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AnimationController implements ArmAnimatorService {
    
    /** Current animation playing, or null if no animation is playing */
    private ArmAnimation current;
    
    @Override
    public void update() {
        
    }

    @Override
    public boolean isPlayingAnimation() { return current != null; }
}
