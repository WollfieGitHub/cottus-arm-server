package fr.wollfie.cottus.resources;

import fr.wollfie.cottus.dto.animation.AnimationPreview;
import fr.wollfie.cottus.dto.animation.AnimationRepositoryEntry;
import fr.wollfie.cottus.dto.animation.ArmAnimation;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.animation.pathing.AnimationPrimitive;
import fr.wollfie.cottus.models.animation.preview.AnimationSampler;
import fr.wollfie.cottus.repositories.animation.AnimationRepository;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/api/arm-animation")
public class ArmAnimationResource {
    
    @Inject AnimationSampler animationSampler;
    @Inject AnimationRepository animationRepository;
    
    @POST
    @Path("/preview")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<AnimationPreview> getPreviewFor(
            @QueryParam("nb_points") int nbPoints,
            AnimationPrimitive animation
    ) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            
            try { return animationSampler.sample(animation, nbPoints); } 
            catch (NoSolutionException e) { throw new RuntimeException("The animation has" +
                    " some unreachable points..."); }
        }));
    }
    
    @POST
    @Path("/min-time")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Double> getMinTimeSecFor(
            @QueryParam("nb_points") int nbPoints,
            AnimationPrimitive animation
    ) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {

            try { return animationSampler.getMinTimeSec(animation, nbPoints); }
            catch (NoSolutionException e) { throw new RuntimeException("The animation has" +
                    " some unreachable points..."); }
        }));
    }
    
    @GET
    @Path("/list-all")
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<AnimationRepositoryEntry> listAll() {
        return Multi.createFrom().iterable(this.animationRepository.listAllAnimations());
    }
}
