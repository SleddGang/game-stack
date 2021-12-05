package com.sleddgang.gameStackGameServer.handler.handlerSchemas;

import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows the game handler to keep track of all the reqids that a client used.
 *
 * @author Benjamin
 */
public class Session extends AbstractHandlerMessage {
    private final WebSocketSession session;
    private final List<Long> reqids;

    public Session(WebSocketSession session) {
        this.session = session;
        this.reqids = new ArrayList<>();
    }

    public WebSocketSession getSession() {
        return this.session;
    }

    public List<Long> getReqids() {
        return this.reqids;
    }
}
