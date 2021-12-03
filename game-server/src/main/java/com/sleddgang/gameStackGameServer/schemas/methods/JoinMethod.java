package com.sleddgang.gameStackGameServer.schemas.methods;

import com.sleddgang.gameStackGameServer.schemas.AbstractGameMethod;
import com.sleddgang.gameStackGameServer.schemas.replies.JoinReply;

/**
 * This event is sent by a client to a game server to attempt to join a match.
 *
 * @see JoinReply
 * @author Benjamin
 */
public class JoinMethod extends AbstractGameMethod {
    /**
     * Uuid of the client that is joining.
     */
    public String clientUuid;

    public JoinMethod(String clientUuid, long reqid) {
        this.clientUuid = clientUuid;
        this.reqid = reqid;
    }

    public JoinMethod() {
        this("", 0);
    }
}
