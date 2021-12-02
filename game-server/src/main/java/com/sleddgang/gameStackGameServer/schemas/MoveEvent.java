package com.sleddgang.gameStackGameServer.schemas;

import com.sleddgang.gameStackGameServer.gameLogic.Option;

/**
 * MoveEvent is sent by a client to the game server to play either rock paper or scissors.
 *
 * @author Benjamin
 */
public class MoveEvent extends GameServerEvent {
    public Option move;

    public MoveEvent(Option move) {
        this.move = move;
    }

    public MoveEvent() {
        this.move = Option.ROCK;
    }
}
