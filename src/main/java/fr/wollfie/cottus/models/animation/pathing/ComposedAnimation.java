package fr.wollfie.cottus.models.animation.pathing;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.wollfie.cottus.dto.animation.AnimationRepositoryEntry;
import fr.wollfie.cottus.dto.animation.ArmAnimation;
import fr.wollfie.cottus.dto.specification.ArmSpecification;
import fr.wollfie.cottus.utils.Preconditions;
import io.smallrye.mutiny.tuples.Tuple;
import io.smallrye.mutiny.tuples.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * A compositions of {@link AnimationPrimitive}s which is serializable, and hence can be saved 
 * as a {@link AnimationRepositoryEntry }
 */
public class ComposedAnimation implements AnimationPrimitive {

    @JsonProperty("animations") private final List<AnimationPrimitive> animations = new ArrayList<>();
    @JsonGetter("animations") public List<AnimationPrimitive> getAnimations() { return animations; }
    
    /** @return The animation responsible for the given timestamp and the timestamp
     * corresponding to the animation's 0 */
    private Tuple2<AnimationPrimitive, Double> getAnimationFrom(double timestamp) {
        double sum = 0;
        AnimationPrimitive animation;
        int i = 0;
        do {
            animation = this.animations.get(i);
            sum += animation.getDurationSecs();
            i++;
        } while (sum < timestamp && i < this.animations.size());
        
        return Tuple2.of(animation, sum-animation.getDurationSecs());
    }
    
    public ComposedAnimation(
            @JsonProperty("animations") List<AnimationPrimitive> animations
    ) {
        Preconditions.checkArgument(animations.size() >= 1);
        this.animations.addAll(animations);
    }

    @Override
    public ArmSpecification evaluateAt(double secFromStart) {
        Tuple2<AnimationPrimitive, Double> animationAndTimestamp = this.getAnimationFrom(secFromStart);
        return animationAndTimestamp.getItem1().evaluateAt(secFromStart - animationAndTimestamp.getItem2());
    }

    @Override
    public double getDurationSecs() {
        return this.animations.stream()
                .map(AnimationPrimitive::getDurationSecs)
                .reduce(Double::sum)
                .get();
    }
}
