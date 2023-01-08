package fr.wollfie.cottus.models.animation.preview;

import fr.wollfie.cottus.dto.animation.AnimationPreview;
import fr.wollfie.cottus.dto.animation.AnimationPreviewPoint;

import java.util.List;

public record AnimationPreviewImpl(
        List<AnimationPreviewPoint> points,
        double duration
) implements AnimationPreview { }
