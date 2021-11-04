package com.sleddgang.gameStackGameServer.schemas;

public class GameServerMessage {
    public String event;
    public long reqid;

    public GameServerMessage(String event, long reqid) {
        this.event = event;
        this.reqid = reqid;
    }

    public GameServerMessage() {
    }
}
