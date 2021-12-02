package com.sleddgang.gameStackGameServer.schemas;

/**
 * This event is sent by the game server to a client in response to a join event.
 * This may not be necessary but it's here.
 *
 * @see JoinEvent
 * @author Benjamin
 */
public class JoinResponse extends GameServerEvent {
    public String matchUuid;

    public JoinResponse(String matchUuid) {
        this.matchUuid = matchUuid;
    }

    public JoinResponse() {
        this.matchUuid = "";
    }
}
