package com.sleddgang.gameStackGameServer.handler.handlerSchemas;

import lombok.Getter;

@Getter
public class ClientReplyMessage extends AbstractHandlerMessage {
    private final String clientUuid;
    private final String server;
    private final long id;

    public ClientReplyMessage(String clientUuid, String server, long id) {
        this.clientUuid = clientUuid;
        this.server = server;
        this.id = id;
    }
}
