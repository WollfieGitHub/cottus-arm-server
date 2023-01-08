package fr.wollfie.cottus.models.animation.preview;

import fr.wollfie.cottus.dto.animation.AnimationPreviewPoint;
import fr.wollfie.cottus.utils.maths.Vector3D;

public record AnimationPreviewPointImpl(
        Vector3D position,
        Vector3D direction,
        double timestamp
) implements AnimationPreviewPoint { }
