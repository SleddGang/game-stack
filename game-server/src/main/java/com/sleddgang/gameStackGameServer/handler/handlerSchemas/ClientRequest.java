package com.sleddgang.gameStackGameServer.handler.handlerSchemas;

import lombok.Getter;

/**
 * ClientRequest is sent from the matchmaking handler to the game handler after a matchmaking server requests a client
 * be added to the authorized clients list.
 *
 * @see ClientResponse
 * @author Benjamin
 */
@Getter
public class ClientRequest extends AbstractHandlerMessage {
    private final String clientUuid;


    public ClientRequest(String clientUuid, String server, long reqid) {
        super(server, reqid);
        this.clientUuid = clientUuid;
    }
}
