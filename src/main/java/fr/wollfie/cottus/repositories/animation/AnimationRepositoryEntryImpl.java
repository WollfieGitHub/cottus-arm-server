package fr.wollfie.cottus.repositories.animation;

import fr.wollfie.cottus.dto.animation.AnimationRepositoryEntry;
import fr.wollfie.cottus.dto.animation.ArmAnimation;

public record AnimationRepositoryEntryImpl(
        String name,
        ArmAnimation animation 
) implements AnimationRepositoryEntry { }
