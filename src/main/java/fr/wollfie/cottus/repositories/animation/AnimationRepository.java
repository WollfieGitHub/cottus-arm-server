package fr.wollfie.cottus.repositories.animation;

import fr.wollfie.cottus.dto.animation.AnimationRepositoryEntry;
import fr.wollfie.cottus.dto.animation.ArmAnimation;
import fr.wollfie.cottus.exception.AnimationNotFoundException;
import fr.wollfie.cottus.models.animation.pathing.AnimationPrimitive;

import java.util.List;

public interface AnimationRepository {

    /** @return The animation that has the given name */
    AnimationRepositoryEntry getAnimationByName(String animationName) throws AnimationNotFoundException;
    
    /** @return The list of all saved animations in the repository */
    List<AnimationRepositoryEntry> listAllAnimations();

    /**
     * Saves the animation in the repository so that it can be found later
     * @param animationName The name under which to save this animation
     * @param animation The animation 
     * @return True if the save was successful
     */
    boolean save(String animationName, AnimationPrimitive animation);
}
