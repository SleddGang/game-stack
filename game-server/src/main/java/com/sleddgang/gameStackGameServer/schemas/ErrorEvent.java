package com.sleddgang.gameStackGameServer.schemas;

public class ErrorEvent extends GameServerEvent {
    public Error error;

    public ErrorEvent(Error error) {
        this.error = error;
    }
    public ErrorEvent() {
        this.error = Error.DEFAULT;
    }
}
