package fr.wollfie.cottus.repositories.animation;

import fr.wollfie.cottus.dto.animation.AnimationRepositoryEntry;
import fr.wollfie.cottus.dto.animation.ArmAnimation;
import fr.wollfie.cottus.exception.AnimationNotFoundException;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class AnimationRepositoryImpl implements AnimationRepository{
    @Override
    public AnimationRepositoryEntry getAnimationByName(String animationName) throws AnimationNotFoundException {
        return null;
    }

    @Override
    public List<AnimationRepositoryEntry> listAll() {
        return null;
    }

    @Override
    public void save(String animationName, ArmAnimation animation) {

    }
}
