package com.sleddgang.gameStackGameServer.handler.shcemas;

public class ErrorMessage extends Message {
    private final HandlerError error;

    public ErrorMessage(HandlerError error) {
        this.error = error;
    }

    public HandlerError getError() {
        return error;
    }
}
