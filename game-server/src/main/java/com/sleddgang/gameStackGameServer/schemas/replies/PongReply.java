package com.sleddgang.gameStackGameServer.schemas.replies;

import com.sleddgang.gameStackGameServer.schemas.AbstractGameMethod;
import com.sleddgang.gameStackGameServer.schemas.AbstractGameReply;
import com.sleddgang.gameStackGameServer.schemas.methods.PingMethod;

/**
 * PongEvent is sent by the game server to a client in response to a PingEvent.
 *
 * @see PingMethod
 * @author Benjamin
 */
public class PongReply extends AbstractGameReply {
    public PongReply(long reqid) {
        super(reqid);
    }

    public PongReply() {
        super();
    }
}
