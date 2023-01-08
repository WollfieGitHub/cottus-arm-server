package fr.wollfie.cottus.models.animation.pathing;

import fr.wollfie.cottus.dto.animation.ArmAnimation;
import fr.wollfie.cottus.dto.specification.ArmSpecification;
import fr.wollfie.cottus.models.arm.positioning.specification.AbsoluteEndEffectorSpecification;
import org.jboss.resteasy.reactive.common.NotImplementedYet;

import java.util.List;

/**
 * An animation that increase the number of frames of an already existing 
 * arm animation, making it smoother with Bézier curves.
 */
public class InterpolatedAnimation implements ArmAnimation {
    
    private final int framesPerSeconds;
    private final ArmAnimation initialAnimation;
    private final List<AbsoluteEndEffectorSpecification> interpolatedFrames;

    /**
     * Creates a new {@link InterpolatedAnimation} with an already existing animation. 
     * Doing so will increase the number of frames of the given animation so that it 
     * reaches the specified {@param framesPerSec} parameter. The newly created frames
     * will result from a Bézier curve between three consecutive frames, which will also
     * results in a smoother animation
     * @param toInterpolate The animation to interpolate
     * @param framesPerSeconds The frames per seconds of the new animation
     */
    public InterpolatedAnimation(ArmAnimation toInterpolate, int framesPerSeconds) {
        this.framesPerSeconds = framesPerSeconds;
        this.initialAnimation = toInterpolate;
        
        this.interpolatedFrames = this.interpolate();
    }

    @Override
    public ArmSpecification evaluateAt(double secFromStart) {
        // TODO
        throw new NotImplementedYet();
    }

    @Override
    public double getDurationSecs() {
        return this.initialAnimation.getDurationSecs();
    }

    /**
     * <p>Increase the number of samples by interpolating between each frame like so :</p>
     * <p>
     *     Given three consecutive frames in the animation, that each represent an end
     *     effector position and orientation, creates a Bezier curve between the first and third
     *     frame, having the second frame set as the anchor of the frame.
     * </p>
     * @return The new set of frames to use
     */
    private List<AbsoluteEndEffectorSpecification> interpolate() {
        // TODO
        throw new NotImplementedYet();
    }
    
    
}
