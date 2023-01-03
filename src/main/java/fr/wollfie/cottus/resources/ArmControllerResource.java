package fr.wollfie.cottus.resources;

import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.specification.AbsoluteEndEffectorSpecification;
import fr.wollfie.cottus.services.arm_controller.ArmManualControllerService;
import fr.wollfie.cottus.utils.maths.Axis3D;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/arm-controller")
public class ArmControllerResource {
    
    @Inject
    ArmManualControllerService armManualControllerService;
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> moveArmGiven(AbsoluteEndEffectorSpecification specification) {
        Log.info(specification);
        return Uni.createFrom().item(specification).onItem().transform(b -> {
            try {
                Log.info(b);
                armManualControllerService.moveEndEffectorWith( 
                        b.getEndEffectorPosition(),
                        b.getEndEffectorOrientation(), 
                        b.getEndEffectorAngleRad());
                return Response.ok().build();
            } catch (NoSolutionException e) {
                e.printStackTrace();
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        });
    }

    @POST
    @Path("/angle-diff")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    public Uni<Response> rotateJointBy(@QueryParam("joint") final int jointIndex, final float deltaAngle) {
        return Uni.createFrom().item(() -> {
            try {
                double angle = armManualControllerService.getAngle(jointIndex);
                armManualControllerService.setAngle(jointIndex, angle+deltaAngle);
            } catch (AngleOutOfBoundsException e) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            return Response.ok().build();
        });
    }

    @POST
    @Path("/angle")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    public Uni<Response> setAngle(@QueryParam("joint") final int jointIndex, final float angle) {
        return Uni.createFrom().item(() -> {
            try {
                armManualControllerService.setAngle(jointIndex, angle);
            } catch (AngleOutOfBoundsException e) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            return Response.ok().build();
        });
    }
    
    private static final long IK_RATE_LIMIT_MS = 100;
    private long lastMs = System.currentTimeMillis();
    
    @POST
    @Path("/end-effector/rotate")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    public Uni<Response> rotateEndEffector(@QueryParam("axis") int axisId, float amountMm) {
        return Uni.createFrom().item(() -> {
            // Rate limit the calls to IK module
            long currentMs = System.currentTimeMillis();
            if (currentMs - lastMs < IK_RATE_LIMIT_MS) { return Response.ok().build(); }
            lastMs = currentMs;
            
            try {
                armManualControllerService.moveEndEffectorBy(amountMm, Axis3D.fromId(axisId));
                
            } catch (NoSolutionException e) { return Response.status(Response.Status.BAD_REQUEST).build(); }
            return Response.ok().build();
        });
    }
}
