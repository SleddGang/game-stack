package com.sleddgang.gameStackGameServer.handler.handlerSchemas;

import lombok.Getter;

/**
 * ErrorMessage is used to send any error from the game handler to the match handler.
 *
 * @author Benjamin
 * @see HandlerError
 */
@Getter
public class ErrorMessage extends AbstractHandlerMessage {
    private final HandlerError error;

    /**
     * WebSocket session id of the server to send the error back to.
     */
    private final String server;

    /**
     * Reqid of the error. Should be the same as the message that caused the error.
     */
    private final long reqid;

    public ErrorMessage(HandlerError error, String server, long reqid) {
        this.error = error;
        this.server = server;
        this.reqid = reqid;
    }
}
