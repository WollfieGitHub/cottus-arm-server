package fr.wollfie.cottus.repositories.animation;

import fr.wollfie.cottus.dto.animation.AnimationRepositoryEntry;
import fr.wollfie.cottus.dto.animation.ArmAnimation;
import fr.wollfie.cottus.models.animation.pathing.AnimationPrimitive;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

import java.util.Objects;

@MongoEntity(collection = "animations")
public final class AnimationRepositoryEntryImpl extends PanacheMongoEntity implements AnimationRepositoryEntry {
    
    private String id;
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }


    private final String name;
    private final AnimationPrimitive animation;

    public AnimationRepositoryEntryImpl(
            String name,
            AnimationPrimitive animation
    ) {
        this.name = name;
        this.animation = animation;
    }

    @Override  public String getName() { return name; }
    @Override  public AnimationPrimitive getAnimation() { return animation; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AnimationRepositoryEntryImpl) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.animation, that.animation);
    }

}
