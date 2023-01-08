package fr.wollfie.cottus.resources;

import fr.wollfie.cottus.dto.animation.AnimationPreview;
import fr.wollfie.cottus.dto.animation.AnimationRepositoryEntry;
import fr.wollfie.cottus.dto.animation.ArmAnimation;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.animation.preview.AnimationSampler;
import fr.wollfie.cottus.repositories.animation.AnimationRepository;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/api/arm-animation")
public class ArmAnimationResource {
    
    @Inject AnimationSampler animationSampler;
    @Inject AnimationRepository animationRepository;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<AnimationPreview> getPreviewFor(
            @QueryParam("animation_name") String animationName,
            @QueryParam("nb_points") int nbPoints
    ) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            ArmAnimation animation = animationRepository.getAnimationByName(animationName).animation();
            
            try { return animationSampler.sample(animation, nbPoints); } 
            catch (NoSolutionException e) { throw new RuntimeException(String.format("The animation %s has" +
                    " some unreachable points...", animationName)); }
        }));
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<AnimationRepositoryEntry> listAll() {
        return Multi.createFrom().iterable(this.animationRepository.listAll());
    }
}
