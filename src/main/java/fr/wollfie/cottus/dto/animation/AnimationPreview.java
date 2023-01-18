package fr.wollfie.cottus.dto.animation;

import com.fasterxml.jackson.annotation.JsonGetter;
import fr.wollfie.cottus.models.animation.preview.AnimationSampler;
import fr.wollfie.cottus.services.AnimationSamplerService;

import java.util.List;

/**
 * The preview of an animation, obtained by sampling multiple points {@link AnimationSamplerService}
 */
public interface AnimationPreview {
    
    /** @return The {@link AnimationPreviewPoint}s for this animation, given by the {@link AnimationSampler} */
    @JsonGetter("points") List<AnimationPreviewPoint> points();
    
    /** @return The duration of the animation in seconds */
    @JsonGetter("duration") double duration();
}
