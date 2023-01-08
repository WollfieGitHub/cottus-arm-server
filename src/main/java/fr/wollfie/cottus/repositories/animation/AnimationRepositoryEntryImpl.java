package fr.wollfie.cottus.repositories.animation;

import fr.wollfie.cottus.dto.animation.AnimationRepositoryEntry;
import fr.wollfie.cottus.dto.animation.ArmAnimation;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

import java.util.Objects;

@MongoEntity(collection = "animations")
public final class AnimationRepositoryEntryImpl extends PanacheMongoEntity implements AnimationRepositoryEntry {
    
    private final String name;
    private final ArmAnimation animation;

    public AnimationRepositoryEntryImpl(
            String name,
            ArmAnimation animation
    ) {
        this.name = name;
        this.animation = animation;
    }

    @Override  public String getName() { return name; }
    @Override  public ArmAnimation getAnimation() { return animation; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AnimationRepositoryEntryImpl) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.animation, that.animation);
    }

}
