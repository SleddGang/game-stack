package com.sleddgang.gameStackGameServer.handler.handlerSchemas;

import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows the game handler to keep track of all the reqids that a client used.
 *
 * @author Benjamin
 */
@Getter
public class Session extends AbstractHandlerMessage {
    private final WebSocketSession session;
    private final List<Long> reqids;

    public Session(WebSocketSession session) {
        this.session = session;
        this.reqids = new ArrayList<>();
    }
}
