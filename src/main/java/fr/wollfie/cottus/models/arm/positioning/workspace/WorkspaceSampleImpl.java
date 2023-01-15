package fr.wollfie.cottus.models.arm.positioning.workspace;

import fr.wollfie.cottus.dto.WorkspaceSample;
import fr.wollfie.cottus.utils.maths.Vector3D;

import java.util.Set;

public record WorkspaceSampleImpl(
    Set<Vector3D> points    
) implements WorkspaceSample { }
