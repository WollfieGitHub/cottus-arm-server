package fr.wollfie.cottus.models.animation.pathing;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fr.wollfie.cottus.dto.animation.ArmAnimation;

/** The basic building blocks of an animation, used mainly for serializability and reproducibility */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BezierToAnimation.class, name = "Bezier"),
        @JsonSubTypes.Type(value = ComposedAnimation.class, name = "Composed"),
        @JsonSubTypes.Type(value = LineToAnimation.class, name = "Line"),
        @JsonSubTypes.Type(value = SemiCircleToAnimation.class, name = "Semicircle"),
        @JsonSubTypes.Type(value = WaitAnimation.class, name = "Wait"),
})
public interface AnimationPrimitive extends ArmAnimation { }
