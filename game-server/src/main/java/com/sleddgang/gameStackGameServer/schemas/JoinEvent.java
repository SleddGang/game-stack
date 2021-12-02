package com.sleddgang.gameStackGameServer.schemas;

/**
 * This event is sent by a client to a game server to attempt to join a match.
 *
 * @see JoinResponse
 * @author Benjamin
 */
public class JoinEvent extends GameServerEvent {
    /**
     * Uuid of the client that is joining.
     */
    public String clientUuid;

    public JoinEvent(String clientUuid) {
        this.clientUuid = clientUuid;
    }

    public JoinEvent() {
        this.clientUuid = "";
    }
}
