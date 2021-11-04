package com.sleddgang.gameStackGameServer.schemas;

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
