package com.sleddgang.gameStackGameServer.handler.handlerSchemas;

import lombok.Getter;

/**
 * Used to pass messages from the
 * {@link com.sleddgang.gameStackGameServer.handler.GameServerHandler} to the
 * {@link com.sleddgang.gameStackGameServer.handler.MatchmakingHandler}.
 *
 * @author Benjamin
 */
@Getter
public abstract class AbstractHandlerMessage {
    private final String server;
    private final long reqid;

    protected AbstractHandlerMessage(String server, long reqid) {
        this.server = server;
        this.reqid = reqid;
    }
}
