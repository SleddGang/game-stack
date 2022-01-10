package com.sleddgang.gameStackGameServer.handler;

/**
 * Used to inform the handler of the current state of the WebSocket
 * <p>JOINING if the clients are still joining.</p>
 * <p>PLAYING if only one client has played a move.</p>
 * <p>ENDED if the match has ended without error.</p>
 * <p>ERROR if the match encountered an error.</p>
 *
 * @author Benjamin
 */
//Used to inform the handler of the current status of the server.
public enum MatchStatus {
    JOINING,
    PLAYING,
    ENDED,
    ERROR;
}
