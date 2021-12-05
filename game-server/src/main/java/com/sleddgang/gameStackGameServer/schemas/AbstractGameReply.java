package com.sleddgang.gameStackGameServer.schemas;

/**
 * Replies are a datatype that are sent in response to a method.
 * The replies' id should be the same as the methods reqid.
 *
 * @author Benjamin
 */
public class AbstractGameReply extends AbstractGameMessage{
    public long id;

    public AbstractGameReply(long id) {
        this.id = id;
    }

    public AbstractGameReply() {
        this(0);
    }
}
