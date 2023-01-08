package fr.wollfie.cottus.dto.animation;

import com.fasterxml.jackson.annotation.JsonGetter;

public interface AnimationRepositoryEntry {
    
    /** @return The name associated to this animation */
    @JsonGetter("name") String getName();
    
    /** @return The animation associated to this entry */
    @JsonGetter("animation") ArmAnimation getAnimation();
}
