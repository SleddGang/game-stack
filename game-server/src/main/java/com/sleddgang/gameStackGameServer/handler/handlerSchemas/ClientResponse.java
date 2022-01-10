package com.sleddgang.gameStackGameServer.handler.handlerSchemas;

import lombok.Getter;

/**
 * ClientResponse is sent from the game handler to the matchmaking handler to be sent out to the matchmaking server
 * that requested a client be added.
 *
 * @see ClientRequest
 * @author Benjamin
 */
@Getter
public class ClientResponse extends AbstractHandlerMessage {
    private final String clientUuid;

    public ClientResponse(String clientUuid, String server, long reqid) {
        super(server, reqid);
        this.clientUuid = clientUuid;
    }
}
