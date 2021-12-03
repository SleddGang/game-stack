package com.sleddgang.gameStackGameServer.schemas;

/**
 * Methods are sent to the server like a command.
 * The reqid allows a client to know which response from the server goes with which method.
 *
 * @author Benjamin
 */
public class AbstractGameMethod extends AbstractGameMessage {
    public long reqid;

    public AbstractGameMethod(long reqid) {
        this.reqid = reqid;
    }

    public AbstractGameMethod() {
        this(0);
    }
}
