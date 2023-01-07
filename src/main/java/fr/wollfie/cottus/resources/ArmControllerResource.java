package fr.wollfie.cottus.resources;

import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.KinematicsModule;
import fr.wollfie.cottus.models.arm.positioning.specification.AbsoluteEndEffectorSpecification;
import fr.wollfie.cottus.models.arm.positioning.specification.RelativeEndEffectorSpecification;
import fr.wollfie.cottus.services.ArmManipulatorService;
import fr.wollfie.cottus.services.arm_controller.ArmManualControllerService;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/arm-controller")
public class ArmControllerResource {
    
    @Inject ArmManualControllerService armManualControllerService;
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                                       WITH SPECIFICATION                             ||
// ||                                                                                      ||
// \\======================================================================================//
    
    @POST
    @Path("absolute-specification")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> moveArmTo(AbsoluteEndEffectorSpecification specification) {
        return Uni.createFrom().item(specification).onItem().transform(b -> {
            armManualControllerService.moveTo( b );
            return Response.ok().build();
        });
    }

    @POST
    @Path("relative-specification")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> moveArmBy(RelativeEndEffectorSpecification specification) {
        return Uni.createFrom().item(specification).onItem().transform(b -> {
            armManualControllerService.moveTo( b );
            return Response.ok().build();
        });
    }
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                                       WITH JOINT ANGLE                               ||
// ||                                                                                      ||
// \\======================================================================================//

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
    
}
