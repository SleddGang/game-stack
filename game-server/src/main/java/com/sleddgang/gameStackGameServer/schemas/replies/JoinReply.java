package com.sleddgang.gameStackGameServer.schemas.replies;

import com.sleddgang.gameStackGameServer.schemas.AbstractGameMethod;
import com.sleddgang.gameStackGameServer.schemas.AbstractGameReply;
import com.sleddgang.gameStackGameServer.schemas.methods.JoinMethod;

/**
 * This event is sent by the game server to a client in response to a join event.
 * This may not be necessary but it's here.
 *
 * @see JoinMethod
 * @author Benjamin
 */
public class JoinReply extends AbstractGameReply {
    public String matchUuid;

    public JoinReply(String matchUuid, long id) {
        this.matchUuid = matchUuid;
        this.id = id;
    }

    public JoinReply() {
        this("", 0);
    }
}
