package com.sleddgang.gameStackGameServer.handler;

import com.sleddgang.gameStackGameServer.gameLogic.Option;
import com.sleddgang.gameStackGameServer.handler.handlerShcemas.Message;
import org.springframework.web.socket.WebSocketSession;

public class Client extends Message {
    private String uuid;
    private WebSocketSession session;
    private Option move = null;
    private long moveReqid = 0;


    public Client(String uuid, WebSocketSession session) {
        this.uuid = uuid;
        this.session = session;
    }

    public String getUuid() {
        return this.uuid;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public String getSessionId() {
        return this.session.getId();
    }

    public Option getMove() {
        return move;
    }

    public long getMoveReqid() {
        return moveReqid;
    }

    public void setMove(Option move, long moveReqid) {
        this.move = move;
        this.moveReqid = moveReqid;
    }
}
