package fr.wollfie.cottus.models.animation.pathing;

import fr.wollfie.cottus.dto.ArmAnimation;
import fr.wollfie.cottus.dto.ArmSpecification;
import fr.wollfie.cottus.models.arm.positioning.specification.AbsoluteEndEffectorSpecification;
import fr.wollfie.cottus.models.arm.positioning.specification.RelativeEndEffectorSpecification;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.smallrye.mutiny.tuples.Tuple;
import io.smallrye.mutiny.tuples.Tuple3;

public abstract class EndEffectorAnimation implements ArmAnimation {

    private boolean firstFrame = true;
    
    /** 
     * Whether the end effector's configuration is given :
     * <ul>
     *     <li>Relative to the end effector's configuration the arm has when it starts this animation</li>
     *     <li>Absolute, given in the global 3D space</li>
     * </ul>
     * */
    protected final boolean relative;

    public EndEffectorAnimation(boolean relative) { this.relative = relative; }

    @Override
    public ArmSpecification evaluateAt(double secFromStart) {
        Tuple3<Vector3D, Rotation, Double> frame = this.relativeEvaluateAt(secFromStart);
        if (this.relative) {
            RelativeEndEffectorSpecification result = new RelativeEndEffectorSpecification(frame.getItem1(), frame.getItem2(), frame.getItem3());
            if (firstFrame) { result.setRoot(true); firstFrame = false; }
            return result;
        } else {
            return new AbsoluteEndEffectorSpecification(frame.getItem1(), frame.getItem2(), frame.getItem3());
        }
    }

    /**
     * Gives a relative position and orientation of the end effector compared to the first frame 
     * of the animation
     * @param secFromStart Seconds from start of the animation
     * @return The end effector specification relative to the first frame of the animation if
     * the animation is relative, or absolute in global space otherwise
     */
    protected abstract Tuple3<Vector3D, Rotation, Double> relativeEvaluateAt(double secFromStart);
}
