package com.sleddgang.gameStackGameServer.handler.handlerSchemas;

import lombok.Getter;

@Getter
public class ClientMessage {
    private final String clientUuid;
    private final String server;
    private final long reqid;

    public ClientMessage(String clientUuid, String server, long reqid) {
        this.clientUuid = clientUuid;
        this.server = server;
        this.reqid = reqid;
    }
}
