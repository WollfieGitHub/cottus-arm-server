package fr.wollfie.cottus.dto.animation;

import com.fasterxml.jackson.annotation.JsonGetter;
import fr.wollfie.cottus.utils.maths.Vector3D;

/**
 * A point in the {@link AnimationPreview} that will be displayed by 
 * the client
 */
public interface AnimationPreviewPoint {
    
    /** @return The position of the animation sample point in 3D Space */
    @JsonGetter("position") Vector3D position();

    /** @return The direction of the animation sample point in 3D Space, i.e., the 
     * end effector's local Z axis */
    @JsonGetter("direction") Vector3D direction();
    
    /** @return The timestamp in the animation at which this point is reached by the end effector */
    @JsonGetter("time") double timestamp();
}
