package com.sleddgang.gameStackGameServer.handler;

import com.sleddgang.gameStackGameServer.gameLogic.Option;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

/**
 * Client contains all the information on a client so that the client can be in a match
 *
 * @see Match
 * @author Benjamin
 */
@Getter
public class Client {
    /**
     * Uuid of the client given out by the matchmaking server.
     */
    private final String uuid;

    /**
     * WebSocket session connected to the client.
     */
    private final WebSocketSession session;

    /**
     * Clients rock paper scissors move. Is null if the client has not made a move.
     */
    @Setter
    private Option move = null;

    /**
     * This constructor creates a client from the client uuid and WebSocket session.
     *
     * @param uuid      Uuid of the client given out by the matchmaking server.
     * @param session   WebSocket session connected to the client.
     */
    public Client(String uuid, WebSocketSession session) {
        this.uuid = uuid;
        this.session = session;
    }

    /**
     * @return  The session id of the clients connected session.
     */
    public String getSessionId() {
        return this.session.getId();
    }
}
