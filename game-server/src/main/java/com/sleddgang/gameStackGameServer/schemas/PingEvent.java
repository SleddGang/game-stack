package com.sleddgang.gameStackGameServer.schemas;

/**
 * PingEvent is sent by a client to the game server in order to test the connection.
 *
 * @see PongEvent
 * @author Benjamin
 */
public class PingEvent extends GameServerEvent {
    public PingEvent() {
    }
}
