package com.sleddgang.gameStackGameServer.handler.handlerSchemas;

/**
 * GetStatusRequest sent from the matchmaking handler to the game handler to request the number of running matches and
 * the client queue size.
 *
 * @see GetStatusRequest
 * @author Benjamin
 */
public class GetStatusRequest extends AbstractHandlerMessage{
    public GetStatusRequest(String server, long reqid) {
        super(server, reqid);
    }
}
