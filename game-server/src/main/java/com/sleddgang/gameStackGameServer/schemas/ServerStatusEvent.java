package com.sleddgang.gameStackGameServer.schemas;

public class ServerStatusEvent extends GameServerEvent {
    public String uuid;
    public int slotsLeft;

    public ServerStatusEvent(String uuid, int slotsLeft) {
        this.uuid = uuid;
        this.slotsLeft = slotsLeft;
    }

    public ServerStatusEvent() {
        this.uuid = "";
        this.slotsLeft = -1;
    }
}
