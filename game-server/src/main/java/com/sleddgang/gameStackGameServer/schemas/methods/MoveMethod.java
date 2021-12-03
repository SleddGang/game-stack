package com.sleddgang.gameStackGameServer.schemas.methods;

import com.sleddgang.gameStackGameServer.gameLogic.Option;
import com.sleddgang.gameStackGameServer.schemas.AbstractGameMethod;

/**
 * MoveEvent is sent by a client to the game server to play either rock paper or scissors.
 *
 * @author Benjamin
 */
public class MoveMethod extends AbstractGameMethod {
    public Option move;

    public MoveMethod(Option move, long reqid) {
        this.move = move;
        this.reqid = reqid;
    }

    public MoveMethod() {
        this(Option.ROCK, 0);
    }
}
