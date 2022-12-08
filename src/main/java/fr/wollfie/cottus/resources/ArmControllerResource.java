package fr.wollfie.cottus.resources;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.services.ArmControllerService;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api/arm-controller")
public class ArmControllerResource {
    
    @Inject ArmControllerService armControllerService;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<CottusArm> getArmState() {
        return Uni.createFrom().item(armControllerService.getArmState());
    }
}
