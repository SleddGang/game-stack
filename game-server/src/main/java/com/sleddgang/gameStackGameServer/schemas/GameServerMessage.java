package com.sleddgang.gameStackGameServer.schemas;

/**
 * WebSocket message sent between either a client or a matchmaking server and the game server.
 * Each message has a reqid and an event.
 *
 * @author Benjamin
 */
public class GameServerMessage {
    public GameServerEvent event;
    public long reqid;

    public GameServerMessage(GameServerEvent event, long reqid) {
        this.event = event;
        this.reqid = reqid;
    }

    public GameServerMessage() {
    }
}
