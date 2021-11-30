package com.sleddgang.gameStackGameServer.handler.handlerShcemas;

public class ErrorMessage extends Message {
    private final HandlerError error;
    private final String server;
    private final long reqid;

    public ErrorMessage(HandlerError error, String server, long reqid) {
        this.error = error;
        this.server = server;
        this.reqid = reqid;
    }

    public HandlerError getError() {
        return error;
    }

    public String getServer() {
        return server;
    }

    public long getReqid() {
        return reqid;
    }
}
