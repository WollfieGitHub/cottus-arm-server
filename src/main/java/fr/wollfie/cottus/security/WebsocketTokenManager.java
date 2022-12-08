package fr.wollfie.cottus.security;

import javax.ws.rs.core.SecurityContext;

public interface WebsocketTokenManager {

    /**
     * Provides a token given a security context. Used to authorize 
     * a secure connection to a websocket
     * @param context The security context of the request
     * @return A valid token if the context is authorized or
     * {@code null} otherwise 
     */
    String getToken(SecurityContext context);

    /**
     * Validate a token issued by this websocketTokenProvider
     * @param token A token to validate
     * @return True if the token is valid, false otherwise
     */
    boolean validate(String token);

    /**
     * Invalidate the specified token so that it is no longer considered as valid
     * @param token The token to invalidate
     */
    void invalidate(String token);
}
