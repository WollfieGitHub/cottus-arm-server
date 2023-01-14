package fr.wollfie.cottus.resources;

import fr.wollfie.cottus.dto.animation.AnimationPreview;
import fr.wollfie.cottus.dto.animation.AnimationRepositoryEntry;
import fr.wollfie.cottus.dto.animation.ArmAnimation;
import fr.wollfie.cottus.exception.AnimationNotFoundException;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.animation.AnimationController;
import fr.wollfie.cottus.models.animation.pathing.AnimationPrimitive;
import fr.wollfie.cottus.models.animation.preview.AnimationSampler;
import fr.wollfie.cottus.repositories.animation.AnimationRepository;
import fr.wollfie.cottus.services.arm_controller.ArmAnimatorControllerService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/arm-animation")
public class ArmAnimationResource {
    
    @Inject AnimationSampler animationSampler;
    @Inject AnimationRepository animationRepository;
    @Inject ArmAnimatorControllerService animationController;
    
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
            catch (NoSolutionException e) { return -1.0; /* Silenced */ }
        }));
    }
    
    @GET
    @Path("/list-all")
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<AnimationRepositoryEntry> listAll() {
        return Multi.createFrom().iterable(this.animationRepository.listAllAnimations());
    }
    
    @POST
    @Path("/save")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Boolean> save(@QueryParam("name") String animationName, AnimationPrimitive animation) {
        return Uni.createFrom().item(animationRepository.save(animationName, animation));
    }
    
    @POST
    @Path("/play")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> play(@QueryParam("name") String animationName) {
        return Uni.createFrom().item(() -> {
            try {
                AnimationRepositoryEntry entry = animationRepository.getAnimationByName(animationName);
                boolean isAnimationAlreadyPlaying = animationController.playAnimation(entry.getAnimation());
                
                
            } catch (AnimationNotFoundException e) { return Response.status(Response.Status.BAD_REQUEST).build(); }
            return Response.ok().build();
        });
    }
}
