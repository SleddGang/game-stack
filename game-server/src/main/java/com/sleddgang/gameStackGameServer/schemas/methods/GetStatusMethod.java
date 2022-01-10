package com.sleddgang.gameStackGameServer.schemas.methods;

import com.sleddgang.gameStackGameServer.schemas.AbstractGameMethod;

/**
 * GetStatusMethod is sent from the matchmaking server to the game server to request the number of running matches
 * as well as the number of clients in queue.
 *
 * @see com.sleddgang.gameStackGameServer.schemas.replies.ServerStatusReply
 * @author Benjamin
 */
public class GetStatusMethod extends AbstractGameMethod {
    public GetStatusMethod(long reqid) {
        this.reqid = reqid;
    }

    public GetStatusMethod() {
        this.reqid = 0;
    }
}
