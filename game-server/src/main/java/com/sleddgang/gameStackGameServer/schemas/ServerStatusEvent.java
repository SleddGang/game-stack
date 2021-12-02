package com.sleddgang.gameStackGameServer.schemas;

/**
 * This event is sent from a game server to a matchmaking server to inform the matchmaking server of the number of
 * slots left on the game server.
 *
 * @author Benjamin
 */
public class ServerStatusEvent extends GameServerEvent {
    public String uuid;
    public int slotsLeft;

    public ServerStatusEvent(String uuid, int slotsLeft) {
        this.uuid = uuid;
        this.slotsLeft = slotsLeft;
    }

    public ServerStatusEvent() {
        this.uuid = "";
        this.slotsLeft = -1;
    }
}
