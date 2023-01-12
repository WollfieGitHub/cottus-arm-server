package fr.wollfie.cottus.repositories.animation;

import fr.wollfie.cottus.dto.animation.AnimationRepositoryEntry;
import fr.wollfie.cottus.dto.animation.ArmAnimation;
import fr.wollfie.cottus.exception.AnimationNotFoundException;
import fr.wollfie.cottus.models.animation.pathing.AnimationPrimitive;
import io.quarkus.logging.Log;
import io.quarkus.mongodb.panache.PanacheMongoRepository;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class AnimationRepositoryImpl implements AnimationRepository, PanacheMongoRepository<AnimationRepositoryEntryImpl> {
   
    @Override
    public AnimationRepositoryEntryImpl getAnimationByName(String animationName) throws AnimationNotFoundException {
        AnimationRepositoryEntryImpl res = find("name", animationName).firstResult();
        if (res == null) { throw new AnimationNotFoundException(); }
        return res;
    }

    @Override
    public List<AnimationRepositoryEntry> listAllAnimations() {
        return listAll().stream().map(e -> (AnimationRepositoryEntry)e).toList();
    }

    @Override
    public boolean save(String animationName, AnimationPrimitive animation) {
        persistOrUpdate(new AnimationRepositoryEntryImpl(animationName, animation));
        return true;
    }
}
