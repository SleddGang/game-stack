package com.sleddgang.gameStackGameServer.handler.handlerSchemas;

import lombok.Getter;

/**
 * ErrorMessage is used to send any error from the game handler to the match handler.
 *
 * @author Benjamin
 * @see HandlerError
 */
@Getter
public class ErrorResponse extends AbstractHandlerMessage {
    private final HandlerError error;

    /**
     * Reqid of the error. Should be the same as the message that caused the error.
     */
    private final long reqid;

    public ErrorResponse(HandlerError error, String server, long reqid) {
        super(server, reqid);
        this.error = error;
        this.reqid = reqid;
    }
}
