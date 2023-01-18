package fr.wollfie.cottus.dto.animation;

import com.fasterxml.jackson.annotation.JsonGetter;
import fr.wollfie.cottus.repositories.animation.AnimationRepository;

/**
 * An entry in the {@link AnimationRepository},
 * contains the name under which the animation is registered, and the {@link ArmAnimation}
 */
public interface AnimationRepositoryEntry {
    
    /** @return The name associated to this animation */
    @JsonGetter("name") String getName();
    
    /** @return The animation associated to this entry */
    @JsonGetter("animation") ArmAnimation getAnimation();
}
