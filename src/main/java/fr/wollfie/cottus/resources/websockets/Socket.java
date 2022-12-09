﻿package fr.wollfie.cottus.resources.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.wollfie.cottus.security.WebsocketTokenManager;
import io.quarkus.logging.Log;
import io.quarkus.security.UnauthorizedException;
import io.quarkus.vertx.ConsumeEvent;


import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * General purpose sock which broadcasts the state of the system everytime
 * a STATE_CHANGED event is fired
 */
@ServerEndpoint(value = "/main-socket/{token}")
public class Socket {

    private final Map<String, Session> connectedClients = new ConcurrentHashMap<>();

    @Inject
    WebsocketTokenManager tokenManager;

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        if (tokenManager.validate(token)) {
            Log.debugf("onOpen> Client connected with token : \"%s\"", token);
            connectedClients.put(token, session);
            
        } else { throwUnauthorizedToken("Connection refused", token); }
    }

    @OnClose
    public void onClose(Session session, @PathParam("token") String token) {
        if (connectedClients.containsKey(token)) {
            Log.info("onClose> " + token);
            connectedClients.remove(token);
            tokenManager.invalidate(token);
            
        } else { throwUnauthorizedToken("A client tried to close its socket", token); }
    }

    @OnError
    public void onError(Session session, @PathParam("token") String token, Throwable throwable) {
        if (connectedClients.containsKey(token)) {
            Log.errorf("onError> \"%s\" : \"%s\"", token, throwable);
            tokenManager.invalidate(token);
            
        } else { throwUnauthorizedToken("A client had an error on its socket", token); }
    }

    @OnMessage
    public void onMessage(String message, @PathParam("token") String token) {
        if (connectedClients.containsKey(token)) {
            Log.info("onMessage> " + token + ": " + message);
            
        } else { throwUnauthorizedToken("A client tried to send a message with its socket", token); }
    }
    
    private void throwUnauthorizedToken(String context, String token) {
        throw new UnauthorizedException(String.format("%s," +
                " unauthorized token : \"%s\"", context, token));
    }

    @Inject
    ObjectMapper defaultObjectMapper;

    // @ConsumeEvent(EventTopics.STATE_UPDATE)
    public void broadcast(double timeSec) {
        // Broadcast a change of state in the system
        connectedClients.values().forEach(client -> {
            // try {
            //      client.getAsyncRemote().sendObject(defaultObjectMapper.writeValueAsString(stateManager.getState()));
            // } catch (JsonProcessingException e) { throw new RuntimeException(e); }
        } );
    }
}
