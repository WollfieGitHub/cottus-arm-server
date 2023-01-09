package fr.wollfie.cottus.repositories.animation;

import fr.wollfie.cottus.dto.animation.AnimationRepositoryEntry;
import fr.wollfie.cottus.dto.animation.ArmAnimation;
import fr.wollfie.cottus.exception.AnimationNotFoundException;
import io.quarkus.mongodb.panache.PanacheMongoRepository;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class AnimationRepositoryImpl implements AnimationRepository, PanacheMongoRepository<AnimationRepositoryEntry> {
   
    @Override
    public AnimationRepositoryEntry getAnimationByName(String animationName) throws AnimationNotFoundException {
        AnimationRepositoryEntry res = find("name", animationName).firstResult();
        
        if (res == null) { throw new AnimationNotFoundException(); }
        return res;
    }

    @Override
    public List<AnimationRepositoryEntry> listAllAnimations() {
        return listAll();
    }

    @Override
    public void save(String animationName, ArmAnimation animation) {
        persistOrUpdate(new AnimationRepositoryEntryImpl(animationName, animation));
    }
}
