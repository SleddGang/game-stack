package com.sleddgang.gameStackGameServer.schemas;

public class JoinResponse extends GameServerEvent {
    public String matchUuid;

    public JoinResponse(String matchUuid) {
        this.matchUuid = matchUuid;
    }

    public JoinResponse() {
        this.matchUuid = "";
    }
}
