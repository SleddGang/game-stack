package com.sleddgang.gameStackGameServer.schemas.replies;

import com.sleddgang.gameStackGameServer.schemas.AbstractGameMethod;
import com.sleddgang.gameStackGameServer.schemas.AbstractGameReply;
import com.sleddgang.gameStackGameServer.schemas.Error;

/**
 * @see Error
 * @author Benjamin
 */
public class ErrorReply extends AbstractGameReply {
    public Error error;

    public ErrorReply(Error error, long id) {
        this.error = error;
        this.id = id;
    }
    public ErrorReply() {
        this(Error.DEFAULT, 0);
    }
}
