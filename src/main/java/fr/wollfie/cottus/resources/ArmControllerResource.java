package fr.wollfie.cottus.resources;

import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.services.arm_controller.ArmManualControllerService;
import fr.wollfie.cottus.utils.maths.Axis3D;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

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
    public Uni<Response> moveArmGiven(JsonObject body) throws NoSolutionException {
        return Uni.createFrom().item(body)
            .onItem().transform(b -> {
                Vector3D pos = Vector3D.of(
                        b.getDouble("xPos"),
                        b.getDouble("yPos"),
                        b.getDouble("zPos"));

                Vector3D euler = Vector3D.of(
                        b.getDouble("eulerX"),
                        b.getDouble("eulerY"),
                        b.getDouble("eulerZ"));

                try {
                    armManualControllerService.moveEndEffectorWith( pos, Rotation.from(euler), b.getDouble("rotRad") );
                    return Response.ok().build();
                } catch (NoSolutionException e) {
                    e.printStackTrace();
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }
            });
    }

    @POST
    @Path("/angle")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    public Uni<Response> setAngle(@QueryParam("n") int n, float angleRad) {
        final int i = n;
        return Uni.createFrom().item(() -> {
            int i2 = i;
            try {
                if (i2 >= 2) { i2+=1; }
                if (i2 >= 5) { i2+=1; }
                if (i2 >= 8) { i2+=1; }
                armManualControllerService.setAngle(i2, angleRad);
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
