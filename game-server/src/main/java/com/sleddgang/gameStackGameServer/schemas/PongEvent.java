package com.sleddgang.gameStackGameServer.schemas;

/**
 * PongEvent is sent by the game server to a client in response to a PingEvent.
 *
 * @see PingEvent
 * @author Benjamin
 */
public class PongEvent extends GameServerEvent {
    public PongEvent() {
    }
}
