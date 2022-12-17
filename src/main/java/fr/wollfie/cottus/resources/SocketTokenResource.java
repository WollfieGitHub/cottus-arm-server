package fr.wollfie.cottus.resources;

import fr.wollfie.cottus.security.WebsocketTokenManager;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

@Path("/api/socket-token")
public class SocketTokenResource {
    
    @Inject
    WebsocketTokenManager tokenProvider;
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getSocketToken(@Context SecurityContext context) {
        return tokenProvider.getToken(context);
    }
    
}
