package com.sleddgang.gameStackGameServer.schemas;

public class JoinEvent extends GameServerEvent {
    public String clientUuid;

    public JoinEvent(String clientUuid) {
        this.clientUuid = clientUuid;
    }

    public JoinEvent() {
        this.clientUuid = "";
    }
}
