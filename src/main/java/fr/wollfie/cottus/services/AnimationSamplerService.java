package fr.wollfie.cottus.services;

import fr.wollfie.cottus.dto.animation.AnimationPreview;
import fr.wollfie.cottus.dto.animation.ArmAnimation;
import fr.wollfie.cottus.exception.NoSolutionException;

public interface AnimationSamplerService {

    /**
     * Creates a set of sampled points from the animation, representing the 
     * end effector's position and direction
     * @param animation The animation to sample
     * @param nbPoints The number of points to create
     * @return The sampled animation to preview
     */
    AnimationPreview sample(ArmAnimation animation, int nbPoints) throws NoSolutionException;
    
}
