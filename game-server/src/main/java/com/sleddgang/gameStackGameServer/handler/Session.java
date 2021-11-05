package com.sleddgang.gameStackGameServer.handler;

import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

public class Session {
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
