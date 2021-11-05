package com.sleddgang.gameStackGameServer.schemas;

public class CreateGameEvent extends GameServerEvent {
    public String uuid;
    public String[] clients;

    public CreateGameEvent(String uuid, String[] clients) {
        this.uuid = uuid;
        this.clients = clients;
    }

    public CreateGameEvent() {
        this.uuid = "";
        this.clients = new String[2];
    }
}
