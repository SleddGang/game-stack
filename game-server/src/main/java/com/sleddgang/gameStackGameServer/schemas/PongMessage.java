package com.sleddgang.gameStackGameServer.schemas;

/**
 * PongMessage is a GameServerMessage sent in response to a PingEvent.
 *
 * @see PingEvent
 * @see PongEvent
 * @author Benjamin
 */
public class PongMessage extends GameServerMessage{
    public PongMessage(long reqid) {
        this.reqid = reqid;
        this.event = new PongEvent();
    }
}
