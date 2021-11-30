package com.sleddgang.gameStackGameServer.schemas;

import com.sleddgang.gameStackGameServer.gameLogic.Option;

public class MoveEvent extends GameServerEvent {
    public Option move;

    public MoveEvent(Option move) {
        this.move = move;
    }

    public MoveEvent() {
        this.move = Option.ROCK;
    }
}
