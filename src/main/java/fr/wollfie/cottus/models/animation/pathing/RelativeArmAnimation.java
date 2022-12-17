package fr.wollfie.cottus.models.animation.pathing;

import fr.wollfie.cottus.dto.ArmAnimation;
import fr.wollfie.cottus.dto.ArmSpecification;
import fr.wollfie.cottus.models.arm.positioning.specification.RelativeEndEffectorSpecification;

public abstract class RelativeArmAnimation implements ArmAnimation {

    private boolean firstFrame = true;
    
    @Override
    public ArmSpecification evaluateAt(double secFromStart) {
        RelativeEndEffectorSpecification result = this.relativeEvaluateAt(secFromStart);
        if (firstFrame) { result.setRoot(true); firstFrame = false; }
        return result;
    }

    /**
     * Gives a relative position and orientation of the end effector compared to the first frame 
     * of the animation
     * @param secFromStart Seconds from start of the animation
     * @return The end effector specification relative to the first frame of the animation 
     */
    protected abstract RelativeEndEffectorSpecification relativeEvaluateAt(double secFromStart);
}
