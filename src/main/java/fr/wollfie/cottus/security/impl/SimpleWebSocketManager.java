package fr.wollfie.cottus.security.impl;

import fr.wollfie.cottus.security.WebsocketTokenManager;
import io.vertx.core.impl.ConcurrentHashSet;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.SecurityContext;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class SimpleWebSocketManager implements WebsocketTokenManager {

    private final Set<String> validTokens = new ConcurrentHashSet<>();

    @Override
    public String getToken(SecurityContext context) {
        String newToken = UUID.randomUUID().toString();
        validTokens.add(newToken);
        return newToken;
    }

    @Override
    public boolean validate(String token) {
        return validTokens.contains(token);
    }

    @Override
    public void invalidate(String token) {
        validTokens.remove(token);
    }
}
