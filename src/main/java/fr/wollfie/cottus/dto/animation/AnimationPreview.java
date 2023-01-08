package fr.wollfie.cottus.dto.animation;

import com.fasterxml.jackson.annotation.JsonGetter;
import fr.wollfie.cottus.models.animation.preview.AnimationSampler;

import java.util.List;

public interface AnimationPreview {
    
    /** @return The {@link AnimationPreviewPoint}s for this animation, given by the {@link AnimationSampler} */
    @JsonGetter("points") List<AnimationPreviewPoint> points();
    
    /** @return The duration of the animation in seconds */
    @JsonGetter("duration") double duration();
}
