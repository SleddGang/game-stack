package com.sleddgang.gameStackGameServer.schemas;

public class PongMessage extends GameServerMessage{
    public PongMessage(long reqid) {
        this.reqid = reqid;
        this.event = new PongEvent();
    }
}
