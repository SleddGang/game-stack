package com.sleddgang.gameStackGameServer.schemas;

public class ErrorEvent extends GameServerEvent {
    public int id;
    public String message;

    public ErrorEvent(int id, String message) {
        this.id = id;
        this.message = message;
    }
    public ErrorEvent() {
        this.id = 0;
        this.message = "";
    }
}
