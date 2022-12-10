package fr.wollfie.cottus.resources;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.services.ArmControllerService;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/api/arm-controller")
public class ArmControllerResource {
    
    @Inject ArmControllerService armControllerService;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<CottusArm> getArmState() {
        return Uni.createFrom().item(armControllerService.getArmState());
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void moveArmGiven(JsonObject body) throws NoSolutionException {
        Vector3D pos = Vector3D.of(
                body.getDouble("xPos"),
                body.getDouble("yPos"),
                body.getDouble("zPos"));

        Vector3D euler = Vector3D.of(
                body.getDouble("eulerX"),
                body.getDouble("eulerY"),
                body.getDouble("eulerZ"));
        
        armControllerService.moveEndEffectorWith( pos, Rotation.from(euler), body.getDouble("rotRad") );
    }
}
